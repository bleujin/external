package net.ion.framework.db.manager;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.ion.external.ICSSubCraken;
import junit.framework.TestCase;

public class TestCrakenScript extends TestCase {

	public void testHasFn() throws Exception {
		
		ICSSubCraken craken = ICSSubCraken.create();
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		CrakenScript cs = CrakenScript.create(craken, ses) ;
		cs.readDir(new File("./resource/js")) ;
		
		
		assertEquals(true, cs.hasFn("Sample@selectby")); 
		assertEquals(false, cs.hasFn("Sample@Unknown")); 
	}
}
