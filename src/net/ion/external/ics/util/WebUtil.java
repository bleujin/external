package net.ion.external.ics.util;

import net.ion.framework.util.FileUtil;

import java.io.File;

public class WebUtil {

	public final static boolean isStaticResource(String uri){
		if (uri == null) return false ;
		
		return uri.startsWith("/css/") || uri.startsWith("/img/") || uri.startsWith("/favicon.ico") || uri.startsWith("/fonts/") || uri.startsWith("/js/") ;
	}
	
	
	public static File[] findFiles(String parentDir, boolean recursive, String... ext){
		File dir = new File(parentDir) ;
		if (! dir.isDirectory()) return new File[0] ;
		return FileUtil.listFiles(dir, ext, recursive).toArray(new File[0]) ;
	}
	
	
//	public static JsonArray findLoaderScripts(){
//		JsonArray result = new JsonArray() ;
//		for (File file : findFiles(WebApp.LOADER_SCRIPT_DIR, true, "script")) {
//			result.add(new JsonPrimitive(file.getName())) ;
//		}
//		return result ;
//	}
//
//	public static String viewLoaderScript(String fileName) throws IOException {
//		return IOUtil.toStringWithClose(new FileInputStream(new File(Webapp.LOADER_SCRIPT_DIR, fileName))) ;
//	}
//
//
//
//	public static JsonArray findScripts(){
//		JsonArray result = new JsonArray() ;
//		for (File file : findFiles(Webapp.SCRIPT_DIR, true, "script")) {
//			result.add(new JsonPrimitive(file.getName())) ;
//		}
//		return result ;
//	}
//
//	public static String viewScript(String fileName) throws IOException{
//		return IOUtil.toStringWithClose(new FileInputStream(new File(Webapp.SCRIPT_DIR, fileName))) ;
//	}
//
//	public static JsonArray findSearchHandlers(){
//		JsonArray result = new JsonArray() ;
//		for (File file : findFiles(Webapp.SEARCH_HANDLER_DIR, true, "handler")) {
//			result.add(new JsonPrimitive(file.getName())) ;
//		}
//		return result ;
//	}
//
//	public static String viewSearchHandler(String fileName) throws IOException{
//		return IOUtil.toStringWithClose(new FileInputStream(new File(Webapp.SEARCH_HANDLER_DIR, fileName))) ;
//	}
//
//	public static JsonArray findSearchTemplates(){
//		JsonArray result = new JsonArray() ;
//		for (File file : findFiles(Webapp.SEARCH_TEMPLAGE_DIR, true, "template")) {
//			result.add(new JsonPrimitive(file.getName())) ;
//		}
//		return result ;
//	}
//
//	public static String viewSearchTemplate(String fileName) throws IOException{
//		return IOUtil.toStringWithClose(new FileInputStream(new File(Webapp.SEARCH_TEMPLAGE_DIR, fileName))) ;
//	}

}
