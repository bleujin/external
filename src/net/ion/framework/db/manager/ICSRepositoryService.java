package net.ion.framework.db.manager;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.procedure.IUserCommand;
import net.ion.framework.db.procedure.IUserCommandBatch;
import net.ion.framework.db.procedure.IUserProcedure;
import net.ion.framework.db.procedure.IUserProcedureBatch;
import net.ion.framework.db.procedure.RepositoryService;

public class ICSRepositoryService extends RepositoryService{

	private RepositoryService dbmrs;
	private RepositoryService smrs;

	public ICSRepositoryService(DBManager dbm, ScriptManager sm) {
		this.dbmrs = dbm.getRepositoryService() ;
		this.smrs = sm.getRepositoryService() ;
	}

	@Override
	public IUserCommand createUserCommand(IDBController idc, String psql) {
		return dbmrs.createUserCommand(idc, psql);
	}

	@Override
	public IUserCommandBatch createUserCommandBatch(IDBController idc, String psql) {
		return dbmrs.createUserCommandBatch(idc, psql);
	}

	@Override
	public IUserProcedure createUserProcedure(IDBController idc, String psql) {
		return new ICSUserProcedure(idc, psql, dbmrs.createUserProcedure(idc, psql), smrs.createUserProcedure(idc, psql));
	}

	@Override
	public IUserProcedureBatch createUserProcedureBatch(IDBController idc, String psql) {
		return new ICSUserProcedureBatch(idc, psql, dbmrs.createUserProcedureBatch(idc, psql), smrs.createUserProcedureBatch(idc, psql));
	}

}