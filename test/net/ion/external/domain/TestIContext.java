package net.ion.external.domain;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang.reflect.MethodUtils;

import net.ion.framework.db.IDBController;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;
import junit.framework.TestCase;

public class TestIContext extends TestCase {

	public void testCallMethod() throws Exception {
		IContext ic = new TestContext() ;
		IDBController result = ic.callAttribute(IDBController.class) ;
		
		assertTrue(result == null);
	}
}


class TestContext implements IContext {

	private Map<String, Object> store = MapUtil.newMap() ;
	
	@Override
	public <T> T getAttribute(Class<T> clz) {
		return (T) store.get(clz.getCanonicalName());
	}
	
	public void putAttribute(Class clz, Object value) {
		store.put(clz.getCanonicalName(), value) ;
	}
	
	
	public <T> T callAttribute(Class<T> clz) {
		try {
			return (T) MethodUtils.invokeMethod(this, "get" + clz.getSimpleName(), new Object[0]) ;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e) ;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e) ;
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e) ;
		}
	}
	
	public IDBController getIDBController(){
		Debug.line("called");
		return null ;
	}
	
}
