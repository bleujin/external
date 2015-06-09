package net.ion.external;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.store.WorkspaceConfigBuilder;
import net.ion.external.config.ESConfig;

import org.apache.lucene.index.CorruptIndexException;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;


public class ICSSubCraken {

    public static final String EntryName = "craken";
    private Craken repository;
	private String wsName;
	private ICSSubCraken(Craken repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
	}

	public static ICSSubCraken create(Craken repository, String wsName, ESConfig esConfig) {
		return new ICSSubCraken(repository, wsName);
	}

	public static ICSSubCraken single() throws IOException{
		Craken r = Craken.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.indexDir("./resource/ics")) ;
		
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
		
		Craken r = Craken.create(dcm, "emanon");
		r.createWorkspace("ics", WorkspaceConfigBuilder.indexDir("./resource/ics").distMode(econfig.serverConfig().cacheMode())) ;
		
		return new ICSSubCraken(r, "ics") ;
	}
	

	public static ICSSubCraken test() throws CorruptIndexException, IOException{
		return new ICSSubCraken(Craken.inmemoryCreateWithTest(), "test") ;
	}
	
	public ReadSession login() throws IOException{
		return repository.login(this.wsName) ;
	}
	
	public ICSSubCraken stop(){
		repository.shutdown() ;
		return this ;
	}

	
}

