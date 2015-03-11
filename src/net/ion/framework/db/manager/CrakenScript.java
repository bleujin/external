package net.ion.framework.db.manager;

import java.io.File;
import java.io.IOException;
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

import net.ion.craken.ICSCraken;
import net.ion.craken.node.ReadSession;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.script.FileAlterationMonitor;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import sun.org.mozilla.javascript.internal.NativeObject;

public class CrakenScript {

	private ScriptEngine sengine;
	private Map<String, Object> packages = MapUtil.newCaseInsensitiveMap();
	private FileAlterationMonitor monitor;
	private ScheduledExecutorService ses;

	CrakenScript(ReadSession session, ScheduledExecutorService ses) {
		ScriptEngineManager manager = new ScriptEngineManager();
		this.ses = ses ;
		this.sengine = manager.getEngineByName("JavaScript");
		sengine.put("session", session);
	}

	public static CrakenScript create(ICSCraken craken, ScheduledExecutorService ses) throws IOException {
		return new CrakenScript(craken.login(), ses);
	}

	public CrakenScript readDir(final File scriptDir) throws IOException {
		return readDir(scriptDir, false) ;
	}
	
	public CrakenScript readDir(final File scriptDir, boolean reloadWhenDetected) throws IOException {
		if (!scriptDir.exists() || !scriptDir.isDirectory())
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
			String script = FileUtil.readFileToString(file);
			String packName = FilenameUtils.getBaseName(file.getName());
			packages.put(packName, sengine.eval(script));
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

	public Object callFn(String uptName, Object... params) throws SQLException{
		try {
			String packName = StringUtil.substringBefore(uptName, "@");
			String fnName = StringUtil.lowerCase(StringUtil.substringAfter(uptName, "@"));

			Object pack = packages.get(packName);
			if (pack == null)
				throw new SQLException("not found package");

			Object result = ((Invocable) sengine).invokeMethod(pack, fnName, params);
			return result;
		} catch (ScriptException e) {
			throw new SQLException(e);
		} catch (NoSuchMethodException e) {
			throw new SQLException(e);
		}
	}

	public boolean hasFn(String uptName) {
		String packName = StringUtil.substringBefore(uptName, "@");
		String fnName = StringUtil.lowerCase(StringUtil.substringAfter(uptName, "@"));

		
		Object pack = packages.get(packName);
		if (pack == null) return false ;
		
		NativeObject no = (NativeObject) pack ;
		return ArrayUtil.contains(no.getAllIds(), fnName) ;
	}

}
