package net.ion.external.ics.setup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.external.ICSSubCraken;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.ListUtil;

public class TestSetup extends TestCase {

	protected ICSSubCraken craken;
	protected DBController dc;
	protected ReadSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.craken = ICSSubCraken.create();
		this.session = craken.login();
		OracleDBManager dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
		this.dc = new DBController(dbm);
		dc.initSelf();
	}

	@Override
	protected void tearDown() throws Exception {
		dc.destroySelf();
		craken.stop();
		super.tearDown();
	}

	public void resetChildren(final String fqn) throws Exception {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy(fqn).removeChildren() ;
				return null;
			}
		}) ;
	}

	
	protected Integer saveToCraken(String procSQL) throws SQLException {
		Integer result = dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				final AtomicInteger count = new AtomicInteger() ;
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						int colCount = rs.getMetaData().getColumnCount() ;
						List<String> cols = ListUtil.newList() ;
						for (int i = 1; i <= colCount; i++) {
							cols.add(rs.getMetaData().getColumnLabel(i).toLowerCase()) ;
						}
						
						while(rs.next()){
							WriteNode wnode = wsession.pathBy(rs.getString(1)) ;
							for (int i = 1; i < cols.size(); i++) {
								wnode.property(cols.get(i), rs.getString(i+1)) ;
							}
							count.incrementAndGet() ;
						}
						return null;
					}
				}) ;
				return count.get();
			}
		}) ;
		return result;
	}
}
