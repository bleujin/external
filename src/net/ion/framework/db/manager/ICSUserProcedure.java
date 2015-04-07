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
	private ScriptManager smanager ;
	private ICSUserProcedures uptsTran;

	public ICSUserProcedure(IDBController idc, String psql, IUserProcedure dbupt, ScriptManager smanager) {
		super(idc, psql) ;
		this.dbupt = dbupt ;
		this.smanager = smanager ;
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
	public int myUpdate(final Connection conn) throws SQLException {
		dbupt.setParamValues(this.getParams(), this.getTypes());

		int result = dbupt.myUpdate(conn);
		if (! smanager.hasFn(getProcName())) return result ;

		IUserProcedure smupt = smanager.getRepositoryService().createUserProcedure(this.getDBController(), getProcName()) ;
		smupt.setParamValues(this.getParams(), this.getTypes());
		if (this.uptsTran != null){
			uptsTran.add(smupt) ;
		} else {
			smupt.myUpdate(conn) ;
		}
		
		
//		smanager.runASync(new Runnable(){
//			public void run(){
//				try {
//					IUserProcedure smupt = smanager.getRepositoryService().createUserProcedure(ICSUserProcedure.this.getDBController(), getProcName()) ;
//					smupt.setParamValues(ICSUserProcedure.this.getParams(), ICSUserProcedure.this.getTypes());
//					smupt.myUpdate(conn) ;
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//		});
		
		return result;
	}

	public void ownerTran(ICSUserProcedures uptsTran) {
		this.uptsTran = uptsTran ;
	}

}
