package net.ion.external.ics.web.script;

import java.util.Map;
import java.util.concurrent.Callable;

import net.ion.framework.util.MapUtil;

public class ScriptContext {

	private Map<String, Object> store = MapUtil.newMap() ;
	public final static String EntryName = "contextEntry" ;
	
	private ScriptContext(){
	}
	
	public final static ScriptContext create(){
		return new ScriptContext() ;
	}
	
	public ScriptContext putAttribute(String key, Object value){
		store.put(key, value) ;
		return this ;
	}
	
	public Object getAttribute(String key){
		return store.get(key) ;
	}
	
	
	public synchronized Object getAttribute(String key, Callable<Object> call) throws Exception{
		Object result = store.get(key) ;
		if (result == null){
			result = call.call() ;
			putAttribute(key, result) ;
		}
		return result ;
	}

	public Object remove(String key){
		return store.remove(key) ;
	}
	
	public boolean exist(String key){
		return store.containsKey(key) ;
	}
}
