package net.ion.external.config;

import java.io.IOException;

import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.store.WorkspaceConfigBuilder;
import net.ion.external.ICSSubCraken;

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

	
	public ICSSubCraken createREntry() throws IOException{
		Craken r = Craken.create(new DefaultCacheManager(repoConfig.crakenConfig()), serverConfig.id());
		r.createWorkspace(repoConfig.wsName(), WorkspaceConfigBuilder.indexDir(repoConfig.adminHomeDir()));
		r.start();

		return ICSSubCraken.create(r, repoConfig.wsName(), this);
	}


	public ICSSubCraken testREntry() throws IOException {
		return ICSSubCraken.test() ;
	}
	
}
