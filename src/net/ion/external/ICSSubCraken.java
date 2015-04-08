package net.ion.external;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;
import net.ion.external.config.ESConfig;

import org.apache.lucene.index.CorruptIndexException;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;


public class ICSSubCraken {

    public static final String EntryName = "craken";
    private RepositoryImpl repository;
	private String wsName;
	private ICSSubCraken(RepositoryImpl repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
	}

	public static ICSSubCraken create(RepositoryImpl repository, String wsName, ESConfig esConfig) {
		return new ICSSubCraken(repository, wsName);
	}

	public static ICSSubCraken single() throws IOException{
		RepositoryImpl r = RepositoryImpl.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics")) ;
		
		return new ICSSubCraken(r, "ics") ;
	}

	
	public static ICSSubCraken create(ESConfig econfig) throws IOException{
		GlobalConfiguration gconfig = new GlobalConfigurationBuilder()
		.transport().defaultTransport()
				.clusterName(econfig.serverConfig().clusterName())
				.nodeName("external")
				.addProperty("configurationFile", "./resource/config/craken-udp.xml")
		.transport().asyncTransportExecutor().addProperty("maxThreads", "100").addProperty("threadNamePrefix", "mytransport-thread")
			.build();
		DefaultCacheManager dcm = new DefaultCacheManager(gconfig);
		
		RepositoryImpl r = RepositoryImpl.create(dcm, "emanon");
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics").distMode(econfig.serverConfig().cacheMode())) ;
		
		return new ICSSubCraken(r, "ics") ;
	}
	

	public static ICSSubCraken test() throws CorruptIndexException, IOException{
		return new ICSSubCraken(RepositoryImpl.inmemoryCreateWithTest(), "test") ;
	}
	
	public ReadSession login() throws IOException{
		return repository.login(this.wsName) ;
	}
	
	public ICSSubCraken stop(){
		repository.shutdown() ;
		return this ;
	}

	
}

