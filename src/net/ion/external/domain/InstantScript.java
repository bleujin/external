package net.ion.external.domain;

import java.io.PrintStream;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.ion.framework.util.ArrayUtil;
import sun.org.mozilla.javascript.internal.NativeObject;

public class InstantScript {

	private ScriptEngine sengine;
	private SimpleBindings bindings;
	
	private InstantScript(ScriptEngine sengine) {
		this.sengine = sengine ;
		this.bindings = new SimpleBindings();
		sengine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}

	public final static InstantScript create(){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine sengine = manager.getEngineByName("JavaScript");
		return new InstantScript(sengine) ;
	}

	public InstantScript bind(String name, Object forbind) {
		this.bindings.put(name, forbind) ;
		return this ;
	}

	public Object run(String script, Object... sparam) throws ScriptException, NoSuchMethodException {
		NativeObject pack = (NativeObject) sengine.eval(script) ;
		
		return ((Invocable) sengine).invokeMethod(pack, "handle", sparam);
	}
}
