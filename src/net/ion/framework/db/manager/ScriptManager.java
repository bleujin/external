package net.ion.framework.db.manager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.plaf.ListUI;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Tables;

import net.ion.external.ICSSubCraken;
import net.ion.framework.db.Rows;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;

public class ScriptManager extends AbScriptManager {

	private final CrakenScript cs;
	private ScriptManager(CrakenScript cs) {
		this.cs = cs ;
	}

	public static ScriptManager create(ICSSubCraken craken, ScheduledExecutorService ses, File baseScriptDir) throws IOException {
		CrakenScript cs = CrakenScript.create(craken, ses) ;
		cs.readDir(baseScriptDir) ;
		
		return new ScriptManager(cs) ;
	}

	@Override
	public Rows queryBy(ScriptUserProcedure cupt) throws SQLException {
		return execQuery(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(ScriptUserProcedure cupt) throws SQLException {
		return execUpdate(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(ScriptUserProcedureBatch cupt) throws SQLException {
		return execUpdateBatch(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	
	private int execUpdateBatch(String procName, Object[] params) throws SQLException {
		if (!cs.hasFn(procName)) return 0;
		
		
		List<Object[]> values = pivot(params) ;
		int resultSum = 0 ;
		for (Object[] val : values) {
			Object result = cs.callFn(procName, val);
			if (result == null) ;
			if (Integer.class.isInstance(result)) resultSum += (Integer) result ;
			if (Double.class.isInstance(result)) resultSum += ((Double)result).intValue() ;
		}
		
		return resultSum ;
	}

	private Rows execQuery(String procName, Object... params) throws SQLException {
		if (!cs.hasFn(procName)) return null ;
		
		Object result = cs.callFn(procName, params);
		if (Rows.class.isInstance(result))
			return (Rows) result;

		throw new IllegalStateException("illegal return type");
	}

	
	private int execUpdate(String procName, Object... params) throws SQLException{
		if (!cs.hasFn(procName)) return 0;

		Object result = cs.callFn(procName, params);
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
	
	

}
