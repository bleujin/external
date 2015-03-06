package net.ion.framework.db.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.procedure.IUserProcedureBatch;
import net.ion.framework.db.procedure.UserProcedureBatch;

public class ICSUserProcedureBatch extends UserProcedureBatch {

	private IUserProcedureBatch dbupt;
	private IUserProcedureBatch smupt;

	public ICSUserProcedureBatch(IDBController idc, String psql, IUserProcedureBatch dbupt, IUserProcedureBatch smupt) {
		super(idc, psql) ;
		this.dbupt = dbupt ;
		this.smupt = smupt ;
	}

	@Override
	public Statement getStatement() throws SQLException {
		return dbupt.getStatement() ;
	}

	@Override
	public <T> T myHandlerQuery(Connection conn, ResultSetHandler<T> rhandler) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());
		return dbupt.myHandlerQuery(conn, rhandler);
	}

	@Override
	public int myUpdate(Connection conn) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());
		smupt.setParamValues(this.getParams(), this.getTypes());
		
		int result =  dbupt.myUpdate(conn) ;
		smupt.myUpdate(conn) ;
		
		
		return result;
	}


}
