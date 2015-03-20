package net.ion.external;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;
import net.ion.external.config.ESConfig;

import org.apache.lucene.index.CorruptIndexException;


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

	public static ICSSubCraken create() throws IOException{
		RepositoryImpl r = RepositoryImpl.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics")) ;
		
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

