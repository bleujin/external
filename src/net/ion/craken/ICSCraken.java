package net.ion.craken;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;
import net.ion.external.config.ESConfig;

import org.apache.lucene.index.CorruptIndexException;


public class ICSCraken {

    public static final String EntryName = "craken";
    private RepositoryImpl repository;
	private String wsName;
	private ICSCraken(RepositoryImpl repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
	}

	public static ICSCraken create(RepositoryImpl repository, String wsName, ESConfig esConfig) {
		return new ICSCraken(repository, wsName);
	}

	public static ICSCraken create() throws IOException{
		RepositoryImpl r = RepositoryImpl.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics")) ;
		
		return new ICSCraken(r, "ics") ;
	}
	
	public static ICSCraken test() throws CorruptIndexException, IOException{
		return new ICSCraken(RepositoryImpl.inmemoryCreateWithTest(), "test") ;
	}
	
	public ReadSession login() throws IOException{
		return repository.login(this.wsName) ;
	}
	
	public ICSCraken stop(){
		repository.shutdown() ;
		return this ;
	}

	
}

