package net.ion.airkjh;

import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.db.procedure.IParameterQueryable;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;
import net.ion.framework.util.StringUtil;

public class TestUserCommand extends TestCase {
	
	public void testParam() throws Exception {
		OracleDBManager dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
		DBController dc = new DBController(dbm) ;
		
		IParameterQueryable command = dc.createUserCommand("select 'hello '||:user greeting from dual").addParam("user", "airkjh") ;
		Rows rs = command.execQuery();
		rs.next() ;
		Debug.line(rs.getString("greeting")) ;
		
		dc.close();
	}
}
