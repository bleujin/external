package net.ion.external.domain;

import junit.framework.TestCase;

public class TestInstantScript extends TestCase {

	public void testCreate() throws Exception {
		InstantScript is = InstantScript.create() ;
		
		is.run("new function(){ this.handle = function(out) { out.println('hello world'); }  }", System.out) ;
	}

}
