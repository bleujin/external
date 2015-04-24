package net.ion.external.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.ion.craken.node.ReadSession;
import net.ion.external.util.ScriptJDK;
import net.ion.framework.db.manager.script.FileAlterationMonitor;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.SystemUtils;


public class CrakenScriptReal {

	private ScriptEngine sengine;
	private Map<String, Object> packages = MapUtil.newCaseInsensitiveMap();
	private FileAlterationMonitor monitor;
	private ScheduledExecutorService ses;
	private IMirror imirror;

	CrakenScriptReal(ReadSession session, IMirror mproxy, ScheduledExecutorService ses) {
		ScriptEngineManager manager = new ScriptEngineManager();
		this.ses = ses ;
		this.sengine = manager.getEngineByName("JavaScript");
		sengine.put("session", session);
		sengine.put("mirror", mproxy);
		this.imirror = mproxy ;
	}

	public static CrakenScriptReal create(ReadSession rsession, IMirror cmirror, ScheduledExecutorService ses) throws IOException {
		return new CrakenScriptReal(rsession, cmirror, ses);
	}

	
	public CrakenScriptReal readResource(Class baseCls, String... jsNames){
		for (String jsName : jsNames) {
			loadPackageScript(jsName, baseCls.getResourceAsStream(jsName + ".js")) ;
		}
		return this ;
	}
	
	
	public CrakenScriptReal readDir(File scriptDir) throws IOException {
		return readDir(scriptDir, false) ;
	}
	
	public CrakenScriptReal readDir(File scriptDir, boolean reloadWhenDetected) throws IOException {
		if (!scriptDir.exists()) return this ; // ignore
		
		if (!scriptDir.isDirectory())
			throw new IllegalArgumentException(scriptDir + " is not directory");

		try {
			if (this.monitor != null)
			this.monitor.stop();
		} catch (Exception e) {
			throw new IOException(e) ;
		} 
			
			
		new DirectoryWalker<String>(FileFilterUtils.suffixFileFilter(".js"), 1) {
			protected void handleFile(File file, int dept, Collection<String> results) throws IOException {
				String packName = loadPackageScript(file);
				results.add(packName);
			}

			protected boolean handleDirectory(File dir, int depth, Collection results) {
				return true;
			}

			public List<String> loadScript(File scriptDir) throws IOException {
				List<String> result = ListUtil.newList();
				super.walk(scriptDir, result);
				return result;
			}

		}.loadScript(scriptDir);
		
		if (! reloadWhenDetected) return this ;

		
		FileAlterationObserver observer = new FileAlterationObserver(scriptDir, FileFilterUtils.suffixFileFilter(".js")) ;
		observer.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileDelete(File file) {
				Debug.line("Package Deleted", file);
				packages.remove(FilenameUtils.getBaseName(file.getName())) ;
			}
			
			@Override
			public void onFileCreate(File file) {
				Debug.line("Package Created", file);
				loadPackageScript(file) ;
			}
			
			@Override
			public void onFileChange(File file) {
				Debug.line("Package Changed", file);
				loadPackageScript(file) ;
			}
		});
		
		try {
			observer.initialize();

			this.monitor = new FileAlterationMonitor(1000, this.ses, observer) ;
			monitor.start(); 
		} catch (Exception e) {
			throw new IOException(e) ;
		} 

		return this;
	}


	private String loadPackageScript(File file)  {
		try {
			return loadPackageScript(file.getName(), new FileInputStream(file)) ;
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	private String loadPackageScript(String name, InputStream input)  {
		try {
			String script = ScriptJDK.trans(input);
			
			String packName = FilenameUtils.getBaseName(name);
			packages.put(packName, (Object)(sengine.eval(script)));
			return packName;
		} catch (IOException e) {
			throw new IllegalStateException(e) ;
		} catch (ScriptException e) {
			throw new IllegalStateException(e) ;
		}
	}
	

	public Map<String, Object> packages() {
		return Collections.unmodifiableMap(packages);
	}

	public Object callFn(Connection conn, String uptName, Object... params) throws SQLException{
		try {
			
			
			String packName = StringUtil.substringBefore(uptName, "@");
			String fnName = StringUtil.lowerCase(StringUtil.substringAfter(uptName, "@"));

			Object pack = packages.get(packName);
			if (pack == null)
				throw new SQLException("not found package");

			Object pmirror = MethodUtils.invokeMethod(imirror, "instant", conn); // for not share connection
			Object result = ((Invocable) sengine).invokeMethod(pack, matchedFnName(pack, fnName), ArrayUtil.add(params, 0, pmirror));
			return result;
		} catch (ScriptException e) {
			throw new SQLException(e);
		} catch (NoSuchMethodException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (InvocationTargetException e) {
			throw new SQLException(e);
		}
	}

	public boolean hasFn(String uptName) {
		String packName = StringUtil.substringBefore(uptName, "@");
		String fnName = StringUtil.lowerCase(StringUtil.substringAfter(uptName, "@"));

		
		Object pack = packages.get(packName);
		if (pack == null) return false ;
		
		return matchedFnName(pack, fnName) != null ;
	}

	
	private String matchedFnName(Object pack, String fnName){
		try {
			Object[] fns = (Object[]) org.apache.commons.lang.reflect.MethodUtils.invokeMethod(pack, "getAllIds", new Object[0]);
			for (Object fn : fns) {
				if (fnName.equalsIgnoreCase(ObjectUtil.toString(fn))) return ObjectUtil.toString(fn) ;
			}
		} catch (NoSuchMethodException e) {
			return null ;
		} catch (IllegalAccessException e) {
			return null ;
		} catch (InvocationTargetException e) {
			return null ;
		}
		return null ;
	}
	
	public void runAsync(Runnable runnable){
		ses.execute(runnable);
	}
}
