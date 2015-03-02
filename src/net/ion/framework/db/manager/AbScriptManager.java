package net.ion.framework.db.manager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import net.ion.craken.node.ReadSession;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.DBManager;
import net.ion.framework.db.procedure.RepositoryService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public abstract class AbScriptManager extends DBManager {

	private Connection fake;
	private RepositoryService cservice;

	protected AbScriptManager() {
		this.cservice = new ScriptRepositoryService(this);
	}
	
	public abstract Rows queryBy(ScriptUserProcedure crakenUserProcedure) throws Exception;

	public abstract int updateWith(ScriptUserProcedure crakenUserProcedure) throws Exception;

	public abstract int updateWith(ScriptUserProcedureBatch crakenUserProcedureBatch) throws Exception;

	

	@Override
	public Connection getConnection() throws SQLException {
		return this.fake;
	}

	@Override
	public int getDBManagerType() {
		return 77;
	}

	@Override
	public String getDBType() {
		return "crakenFn";
	}

	@Override
	public RepositoryService getRepositoryService() {
		return cservice;
	}

	@Override
	protected void myDestroyPool() throws Exception {

	}

	protected void heartbeatQuery(IDBController dc) throws SQLException {
		// no action
	}

	@Override
	protected void myInitPool() throws SQLException {
		Enhancer e = new Enhancer();
		e.setSuperclass(Connection.class);
		e.setCallback(new ConnectionMock());

		this.fake = (Connection) e.create();
	}
	
	
}

class ConnectionMock implements MethodInterceptor {

	@Override
	public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
		return null;
	}
	
}

