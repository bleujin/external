package net.ion.framework.db.manager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import net.ion.craken.node.ReadSession;
import net.ion.external.domain.CrakenScriptReal;
import net.ion.external.domain.IMirror;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.scriptfn.ScriptDummy;
import net.ion.framework.util.ListUtil;

import com.google.common.collect.HashBasedTable;

public class ScriptManager extends AbScriptManager {

	private final CrakenScriptReal cs;
	private ScriptManager(CrakenScriptReal cs) {
		this.cs = cs ;
	}

	public static ScriptManager create(ReadSession rsession, ScheduledExecutorService ses, IMirror mproxy, File baseScriptDir) throws IOException {
		CrakenScriptReal cs = CrakenScriptReal.create(rsession, mproxy, ses) ;
		cs.readDir(baseScriptDir, true) ;
		
		
		cs.readResource(ScriptDummy.class, "article", "content_extend", "content", "gallery", "site_category", "utils") ;
		
		return new ScriptManager(cs) ;
	}

	public ScriptManager readJs(Class baseClz, String... jsNames){
		cs.readResource(baseClz, jsNames) ;
		return this ;
	}
	
	
	@Override
	public Rows queryBy(ScriptUserProcedure cupt, Connection conn) throws SQLException {
		return execQuery(conn, cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(ScriptUserProcedure cupt, Connection conn) throws SQLException {
		return execUpdate(conn, cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(ScriptUserProcedureBatch cupt, Connection conn) throws SQLException {
		return execUpdateBatch(conn, cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	
	public boolean hasFn(String procName){
		return cs.hasFn(procName) ;
	}
	
	
	private int execUpdateBatch(Connection conn, String procName, Object[] params) throws SQLException {
		if (!cs.hasFn(procName)) return 0;
		
		
		List<Object[]> values = pivot(params) ;
		int resultSum = 0 ;
		for (Object[] val : values) {
			Object result = cs.callFn(conn, procName, val);
			if (result == null) ;
			if (Integer.class.isInstance(result)) resultSum += (Integer) result ;
			if (Double.class.isInstance(result)) resultSum += ((Double)result).intValue() ;
		}
		
		return resultSum ;
	}

	private Rows execQuery(Connection conn, String procName, Object... params) throws SQLException {
		if (!cs.hasFn(procName)) return null ;
		
		Object result = cs.callFn(conn, procName, params);
		if (Rows.class.isInstance(result))
			return (Rows) result;

		throw new IllegalStateException("illegal return type");
	}

	
	private int execUpdate(Connection conn, String procName, Object... params) throws SQLException{
		if (!cs.hasFn(procName)) return 0;

		Object result = cs.callFn(conn, procName, params);
		if (result == null) return 0 ;
		if (Integer.class.isInstance(result)) return (Integer) result ;
		if (Double.class.isInstance(result)) return ((Double)result).intValue() ;

		throw new IllegalStateException("illegal return type");
	}

	
	static List<Object[]> pivot(Object[] params)  {
		HashBasedTable<Integer, Integer, Object> tables = HashBasedTable.create() ;
		for(int k = 0, klast = params.length ; k < klast ; k++ ){
			Object[] param = (Object[]) params[k] ;
			for (int i = 0, ilast = param.length ; i < ilast; i++) {
				tables.put(i, k, param[i]) ;
			}
		}
		
		Collection<Map<Integer, Object>> cols = tables.rowMap().values() ;
		Map[] vals = cols.toArray(new Map[0]) ;
		List<Object[]> result = ListUtil.newList() ;
		for (Map val : vals) {
			result.add(val.values().toArray(new Object[0])) ;
		}

		// Debug.line(Tables.transpose(tables).rowMap()) ;
		
		return result ;
		
	}
	
	public void runASync(Runnable runnable){
		cs.runAsync(runnable);
	}

}
