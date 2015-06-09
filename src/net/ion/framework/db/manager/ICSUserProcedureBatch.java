package net.ion.framework.db.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.procedure.IUserProcedure;
import net.ion.framework.db.procedure.IUserProcedureBatch;
import net.ion.framework.db.procedure.UserProcedureBatch;

public class ICSUserProcedureBatch extends UserProcedureBatch {

	private IUserProcedureBatch dbupt;
	private ScriptManager smanager;

	public ICSUserProcedureBatch(IDBController idc, String psql, IUserProcedureBatch dbupt, ScriptManager smanager) {
		super(idc, psql);
		this.dbupt = dbupt;
		this.smanager = smanager;
	}

	@Override
	public Statement getStatement() throws SQLException {
		return dbupt.getStatement();
	}

	@Override
	public <T> T myHandlerQuery(Connection conn, ResultSetHandler<T> rhandler) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());
		return dbupt.myHandlerQuery(conn, rhandler);
	}

	@Override
	public int myUpdate(final Connection conn) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());

		int result = dbupt.myUpdate(conn);
		// smupt.myUpdate(conn) ;

		if (!smanager.hasFn(getProcName()))
			return result;

		smanager.runASync(new Runnable() {
			@Override
			public void run() {
				try {
					ICSUserProcedureBatch that = ICSUserProcedureBatch.this;
					IUserProcedure smupt = smanager.getRepositoryService().createUserProcedureBatch(that.getDBController(), getProcName());
					smupt.setParamValues(that.getParams(), that.getTypes());
					smupt.myUpdate(conn);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		});

		return result;
	}

}
