package net.ion.framework.db.manager.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

public class TestJScriptEngine extends TestCase {

	public void testScriptManger() throws Exception {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		engine.eval("importClass(java.util.Vector)	\n"
					+ "	print(\"Hello world\") ;");
	}
}
