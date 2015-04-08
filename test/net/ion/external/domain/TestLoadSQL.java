package net.ion.external.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import net.ion.cms.env.SQLLoader;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.StringUtil;

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
	

    public void testDyImage() throws Exception {
		String imgSrc = "dddd <img src=\"/ics/galm/gallery.do?forwardName=resource_view&galId=10995\" alt=\"\"> dddddd" ;
		String[] founds = StringUtil.substringsBetween(imgSrc, "/ics/galm/gallery.do?forwardName=resource_view&galId=", "\"");
		Debug.line(founds);
		
		List<String> searchs = ListUtil.newList() ;
		List<String> replaces = ListUtil.newList() ;
		for (String found : founds) {
			searchs.add("/ics/galm/gallery.do?forwardName=resource_view&galId=" + found) ;
            ///{did}/content/{catid}/{artid}/{resourceid}.stream
			replaces.add("/admin/gallery/did/" + found) ;
		}
		
		Debug.line(StringUtil.replaceEachRepeatedly(imgSrc, searchs.toArray(new String[0]), replaces.toArray(new String[0]))) ;
	}
    
}
