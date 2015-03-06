package net.ion.framework.db.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.procedure.IUserProcedure;
import net.ion.framework.db.procedure.UserProcedure;

public class ICSUserProcedure extends UserProcedure {

	private IUserProcedure dbupt;
	private IUserProcedure smupt;

	public ICSUserProcedure(IDBController idc, String psql, IUserProcedure dbupt, IUserProcedure smupt) {
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
	public Rows myQuery(Connection conn) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());
		return dbupt.myQuery(conn) ;
	}

	@Override
	public int myUpdate(Connection conn) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());
		smupt.setParamValues(this.getParams(), this.getTypes());

		int result = dbupt.myUpdate(conn);
		smupt.myUpdate(conn) ;
		return result;
	}

}
