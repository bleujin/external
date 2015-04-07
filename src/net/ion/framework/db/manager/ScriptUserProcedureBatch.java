package net.ion.framework.db.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.procedure.UserProcedureBatch;
import net.ion.framework.util.ObjectUtil;

import org.apache.lucene.index.CorruptIndexException;

public class ScriptUserProcedureBatch extends UserProcedureBatch {

	private AbScriptManager manager ;
	ScriptUserProcedureBatch(IDBController dc, AbScriptManager manager, String procSQL) {
		super(dc, procSQL);
		this.manager = manager ;
	}

	@Override
	public Statement getStatement() throws SQLException {
		throw new UnsupportedOperationException() ;
	}


	@Override
	public int myUpdate(Connection conn) throws SQLException {
		try {
			return manager.updateWith(this, conn);
		} catch (Exception e) {
			throw new SQLException(ObjectUtil.coalesce(e.getCause(), e)) ;
		} 
	}
}
