package net.ion.external.config;

import java.io.IOException;

import net.ion.craken.ICSCraken;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;

import org.infinispan.manager.DefaultCacheManager;


public class ESConfig {

	private ServerConfig serverConfig;
	private LogConfig logConfig;
	private RepositoryConfig repoConfig ;
	
	public ESConfig(ServerConfig serverConfig, RepositoryConfig repoConfig, LogConfig logConfig) {
		this.serverConfig = serverConfig ;
		this.repoConfig = repoConfig ;
		this.logConfig = logConfig;
	}

	
	public ServerConfig serverConfig(){
		return serverConfig ;
	}

	public LogConfig logConfig(){
		return logConfig ;
	}


	public RepositoryConfig repoConfig() {
		return repoConfig ;
	}

	
	public ICSCraken createREntry() throws IOException{
		RepositoryImpl r = RepositoryImpl.create(new DefaultCacheManager(repoConfig.crakenConfig()), serverConfig.id());
		r.createWorkspace(repoConfig.wsName(), WorkspaceConfigBuilder.directory(repoConfig.adminHomeDir()));
		r.start();

		return ICSCraken.create(r, repoConfig.wsName(), this);
	}


	public ICSCraken testREntry() throws IOException {
		return ICSCraken.test() ;
	}
	
}
