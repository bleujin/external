package net.ion.external.domain;

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.Debug;

public class TestLoadSQL extends TestCase{

	public void testSQLLoader() throws Exception {
		SQLLoader loader = SQLLoader.create(getClass().getResourceAsStream("esql.sql")) ;
		//loader.add("abc", "select * from dual") ;
		
		IDBController dc = new DBController(new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6"));
		Debug.line(loader.query(dc, "articles").getProcFullSQL()) ;
		
		loader.query(dc, "articles").addParam("catId", "dynamic").addParam("includeSub", "F")
				.execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
						while(rs.next()){
							Debug.line(rs.getString("artid"));
						}
						return null;
					}
				}) ;
		
		
	}
}
