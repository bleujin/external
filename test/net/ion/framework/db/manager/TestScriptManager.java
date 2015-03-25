package net.ion.framework.db.manager;

import java.io.File;
import java.util.Collection;
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
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
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
		
		ScriptManager sm = ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js")) ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		dc.createUserProcedure("sample@selectBy(?)").addParam(100).execQuery().debugPrint();
		dc.close(); 
	}

	public void testUserProcedureBatch() throws Exception {
		ScriptManager sm = ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js")) ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		dc.createUserProcedureBatch("sample@addBatchWith(?,?)").addParam(new int[]{100, 200, 300}).addParam(new String[]{"bleujin", "hero", "jin"}).execUpdate();
		dc.close();
		
		craken.login().pathBy("/sample").children().debugPrint();
	}
	
	
	public void testIFNotExist() throws Exception {
		ScriptManager sm = ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js")) ;
		DBController dc = new DBController(sm) ;
		dc.initSelf(); 
		
		Rows notRows = dc.createUserProcedure("sample@notfoundBy(?)").addParam(100).execQuery() ;
		assertEquals(true, notRows == null);
		dc.close();  
	}
	
	public void testICSProcedure() throws Exception {
		ICSManager idbm = ICSManager.create(
					new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "bleu", "redf"), 
					ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js"))) ;
		DBController dc = new DBController(idbm) ;
		dc.initSelf();
		
		
		dc.createUserProcedure("sample@deleteWith(?)").addParam(100).execUpdate() ;
		
		dc.createUserProcedure("sample@insertWith(?,?)").addParam(100).addParam("bleujin").execUpdate() ;
		
		assertEquals(1, dc.createUserProcedure("sample@selectBy(?)").addParam(100).execQuery().getRowCount()) ;
		
		craken.login().pathBy("/sample").children().debugPrint();  
		dc.close(); 
	}

	public void testICSProcedureBatch() throws Exception {
		ICSManager idbm = ICSManager.create(
					new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "bleu", "redf"), 
					ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js"))) ;

		DBController dc = new DBController(idbm) ;
		dc.initSelf();

		dc.createUserProcedureBatch("sample@delBatchWith(?)").addParam(new int[]{100, 200}).execUpdate() ;

		IUserProcedureBatch upts = dc.createUserProcedureBatch("sample@addBatchWith(?,?)") ; // .addParam(100).addParam("bleujin").execUpdate() ;
		upts.addParam(new int[]{100, 200}).addParam(new String[]{"bleujin", "hero"}).execUpdate() ;
		

		craken.login().pathBy("/sample").children().debugPrint();  
		dc.close(); 
	}

	
	public void testICSProcedures() throws Exception {

		ICSManager idbm = ICSManager.create(
				new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "bleu", "redf"), 
				ScriptManager.create(craken, Executors.newScheduledThreadPool(1), new File("./resource/js"))) ;

		DBController dc = new DBController(idbm) ;
		dc.initSelf();
	
		dc.createUserProcedureBatch("sample@delBatchWith(?)").addParam(new int[]{100, 200, 300}).execUpdate() ;

		
		IUserProcedures uptcol = dc.createUserProcedures("upts") ;
		uptcol.add(dc.createUserProcedureBatch("sample@addBatchWith(?,?)").addParam(new int[]{100, 200}).addParam(new String[]{"bleujin", "hero"}));
		uptcol.add(dc.createUserProcedureBatch("sample@addBatchWith(?,?)").addParam(new int[]{300}).addParam(new String[]{"jin"})).execUpdate() ;
		
	
		craken.login().pathBy("/sample").children().ascending("a").debugPrint();  
		
		
		dc.close(); 

		
	}
	


}
