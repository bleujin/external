package net.ion.cms.env;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.procedure.IUserCommand;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

public class SQLLoader {

	private Map<String, String> sqlMap;

	private SQLLoader(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap ;
	}

	public static SQLLoader create(InputStream in) throws IOException {
		List<String> lines = IOUtil.readLines(in) ;
		
		SQLLoader result = new SQLLoader(MapUtil.<String>newCaseInsensitiveMap()) ;
		
		String name = "" ;
		StringBuilder procSQL = new StringBuilder() ; 
		for(String line : lines) {
			if (StringUtil.isBlank(line)){
				result.add(name, procSQL.toString()) ;
				name = "" ;
				procSQL = new StringBuilder() ;
			}
			
			if (StringUtil.isBlank(name)){
				name = StringUtil.trim(line) ;
			} else {
				procSQL.append(line + "\r\n") ;
			}
		}
		result.add(name, procSQL.toString()) ;
		return result ;
	}

	public SQLLoader add(String name, String procSQL) {
		if (StringUtil.isBlank(name)) return this ;
		sqlMap.put(name, procSQL) ;
		return this ;
	}

	public boolean has(String name) {
		return sqlMap.containsKey(name) ;
	}

	public IUserCommand query(IDBController dc, String name) throws SQLException {
		if (! has(name)) throw new IllegalArgumentException("not found sql :" + name) ;
		return dc.createUserCommand(sqlMap.get(name)) ;
	}

}
