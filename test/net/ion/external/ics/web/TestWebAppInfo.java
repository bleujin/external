package net.ion.external.ics.web;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import junit.framework.TestCase;
import net.ion.external.ics.misc.MenuWeb;
import net.ion.external.ics.web.anno.Info;
import net.ion.external.ics.web.anno.InfoBean;
import net.ion.external.ics.web.anno.InfoFac;
import net.ion.external.ics.web.anno.MethodInfo;
import net.ion.external.ics.web.domain.ArticleWeb;
import net.ion.external.ics.web.domain.DomainWeb;
import net.ion.external.ics.web.domain.GalleryWeb;
import net.ion.external.ics.web.domain.OpenArticleWeb;
import net.ion.external.ics.web.domain.OpenGalleryWeb;
import net.ion.external.ics.web.icommand.ICommandWeb;
import net.ion.external.ics.web.icommand.OpenICommandWeb;
import net.ion.external.ics.web.misc.CrakenLet;
import net.ion.external.ics.web.misc.ExportWeb;
import net.ion.external.ics.web.misc.MiscWeb;
import net.ion.external.ics.web.misc.TraceWeb;
import net.ion.external.ics.web.script.OpenScriptWeb;
import net.ion.external.ics.web.script.ScriptWeb;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.StringUtil;

import org.apache.commons.lang.SystemUtils;

public class TestWebAppInfo extends TestCase {

	public void testClass() throws Exception {
		List<Class<? extends Webapp>> apps = ListUtil
				.<Class<? extends Webapp>> toList(DomainWeb.class, ICommandWeb.class, ArticleWeb.class, GalleryWeb.class, ScriptWeb.class, MiscWeb.class, TraceWeb.class, MenuWeb.class, CrakenLet.class, ExportWeb.class, OpenArticleWeb.class, OpenGalleryWeb.class, OpenScriptWeb.class, OpenICommandWeb.class);

//		List<Class<? extends Webapp>> apps = ListUtil
//				.<Class<? extends Webapp>> toList(AnalysisWeb.class, IndexerWeb.class);

		
		File parentDir = new File("./resource/api") ;
		if (! parentDir.exists()) parentDir.mkdirs() ;
		for(File apiFile : parentDir.listFiles()){
			apiFile.delete() ;
		}
//		FileUtil.deleteDirectory(parentDir);
//		
//		if (! parentDir.exists()){
//			parentDir.mkdirs() ;
//		}
		
		for (Class<? extends Webapp> appClass : apps) {
			InfoFac<? extends Webapp> winfo = InfoFac.create(appClass);
			InfoBean bean = winfo.visit(Info.DEFAULT);
			
			String prefix = bean.clsName().startsWith("Open") ? "open" : "admin" ;
			String path = prefix + StringUtil.replace(bean.prefixPath(), "/", "_") + ".api";
			  
			File file = new File(parentDir, path) ;
			Debug.line(file);
			FileWriter writer = new FileWriter(file);
			
			List<MethodInfo> methods = bean.methods();
			for (MethodInfo mi : methods) {
				writer.write(mi.description("/" + prefix).toString());
				writer.write(SystemUtils.LINE_SEPARATOR);
			}
			writer.close();
			// break ;
		}

	}
}
