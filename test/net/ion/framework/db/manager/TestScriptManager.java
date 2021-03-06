package net.ion.framework.db.manager;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jgroups.protocols.relay.SiteMaster;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Tables;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.util.TransactionJobs;
import net.ion.external.ICSSubCraken;
import net.ion.external.domain.IMirror;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.scriptfn.ScriptDummy;
import net.ion.framework.db.procedure.IUserCommand;
import net.ion.framework.db.procedure.IUserProcedureBatch;
import net.ion.framework.db.procedure.IUserProcedures;
import net.ion.framework.util.Debug;

public class TestScriptManager extends TestCase {

	
	private ICSSubCraken craken;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.craken = ICSSubCraken.test() ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		craken.stop() ;
		super.tearDown();
	}
	
	
	public void testUserProcedure() throws Exception {
		ReadSession rsession = craken.login() ;
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/sample/100").property("a", 100).property("b", "bvalue") ;
				return null;
			}
		}) ;
		
		ScriptManager sm = ScriptManager.create(craken.login(), Executors.newScheduledThreadPool(1), IMirror.DUMMY, new File("./resource/js")) ;
		sm.readJs(ScriptDummy.class, "sample") ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		dc.createUserProcedure("sample@selectBy(?)").addParam(100).execQuery().debugPrint();
		dc.close(); 
	}

	public void testUserProcedureBatch() throws Exception {
		ScriptManager sm = ScriptManager.create(craken.login(), Executors.newScheduledThreadPool(1), IMirror.DUMMY, new File("./resource/js")) ;
		sm.readJs(ScriptDummy.class, "sample") ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		dc.createUserProcedureBatch("sample@addBatchWith(?,?)").addParam(new int[]{100, 200, 300}).addParam(new String[]{"bleujin", "hero", "jin"}).execUpdate();
		dc.close();
		
		craken.login().pathBy("/sample").children().debugPrint();
	}
	
	
	public void testIFNotExist() throws Exception {
		ScriptManager sm = ScriptManager.create(craken.login(), Executors.newScheduledThreadPool(1), IMirror.DUMMY, new File("./resource/js")) ;
		sm.readJs(ScriptDummy.class, "sample") ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		Rows notRows = dc.createUserProcedure("sample@notfoundBy(?)").addParam(100).execQuery() ;
		assertEquals(true, notRows == null);
		dc.close();  
	}

	


}
