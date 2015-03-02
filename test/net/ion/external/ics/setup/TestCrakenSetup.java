package net.ion.external.ics.setup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import net.ion.craken.Craken;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;

public class TestCrakenSetup extends TestCase {

	
	private Craken craken;
	private DBController dc;
	private ReadSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.craken = Craken.create() ;
		this.session = craken.login() ;
		OracleDBManager dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6") ;
		this.dc = new DBController(dbm) ;
		dc.initSelf(); 
	}
	
	@Override
	protected void tearDown() throws Exception {
		dc.destroySelf();
		craken.stop() ;
		super.tearDown();
	}
	
	public void testGallery() throws Exception {
		String procSQL ="select '/gcat/' || galCatId fqn, galCatId catId, galUpperCatId parent, galCatNm name" +
						" from gallery_category_tblc";
		Integer result = dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				final AtomicInteger count = new AtomicInteger() ;
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).property("catid", rs.getString("catId")).property("parent", rs.getString("parent")).property("name", rs.getString("name")) ;
							count.incrementAndGet() ;
						}
						return null;
					}
				}) ;
				return count.get();
			}
		}) ;
		
		Debug.line(result + " applied");
	}
	
	public void testGalleryReference() throws Exception {
		String procSQL ="select '/gcat/' || galUpperCatId fqn, '/gcat/' ||galCatId child from gallery_category_tblc " +
						" where galUpperCatId != 'root' connect by galUpperCatId = prior galCatId start with galupperCatId = 'root'" ;

		Integer result = dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				final AtomicInteger count = new AtomicInteger() ;
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).refTos("tree", rs.getString("child")) ;
							count.incrementAndGet() ;
						}
						return null;
					}
				}) ;
				return count.get();
			}
		}) ;
		
		Debug.line(result + " applied");

	}

	
	public void testViewGallery() throws Exception {
//		session.pathBy("/gcat/sm").children().debugPrint(); 
		
		session.pathBy("/gcat/sm").walkRefChildren("tree").debugPrint();
	}
	
	
}
