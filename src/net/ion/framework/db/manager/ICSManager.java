package net.ion.framework.db.manager;

import java.sql.Connection;
import java.sql.SQLException;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.procedure.RepositoryService;

public class ICSManager extends DBManager{

	private DBManager dbm;
	private ScriptManager sm;
	private ICSRepositoryService icsrs;

	public ICSManager(DBManager dbm, ScriptManager sm) {
		this.dbm = dbm ;
		this.sm = sm ;
		this.icsrs = new ICSRepositoryService(dbm, sm) ;
	}

	public static ICSManager create(DBManager dbm, ScriptManager sm) {
		return new ICSManager(dbm, sm) ;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dbm.getConnection();
	}

	@Override
	public int getDBManagerType() {
		return dbm.getDBManagerType() + sm.getDBManagerType() ;
	}

	@Override
	public String getDBType() {
		return dbm.getDBType() + sm.getDBType();
	}

	@Override
	public RepositoryService getRepositoryService() {
		return icsrs;
	}

	@Override
	protected void myDestroyPool() throws Exception {
		dbm.myDestroyPool(); 
		sm.myDestroyPool(); 
	}

	@Override
	protected void myInitPool() throws SQLException {
		dbm.myInitPool(); 
		sm.myInitPool(); 
	}
	
	@Override
	protected void heartbeatQuery(IDBController dc) throws SQLException{
		dbm.heartbeatQuery(dc);
	}

}
