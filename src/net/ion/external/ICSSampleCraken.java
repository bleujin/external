package net.ion.external;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;
import net.ion.external.config.ESConfig;

import org.apache.lucene.index.CorruptIndexException;


public class ICSSampleCraken {

    public static final String EntryName = "craken";
    private RepositoryImpl repository;
	private String wsName;
	private ICSSampleCraken(RepositoryImpl repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
	}

	public static ICSSampleCraken create(RepositoryImpl repository, String wsName, ESConfig esConfig) {
		return new ICSSampleCraken(repository, wsName);
	}

	public static ICSSampleCraken create() throws IOException{
		RepositoryImpl r = RepositoryImpl.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics")) ;
		
		return new ICSSampleCraken(r, "ics") ;
	}
	
	public static ICSSampleCraken test() throws CorruptIndexException, IOException{
		return new ICSSampleCraken(RepositoryImpl.inmemoryCreateWithTest(), "test") ;
	}
	
	public ReadSession login() throws IOException{
		return repository.login(this.wsName) ;
	}
	
	public ICSSampleCraken stop(){
		repository.shutdown() ;
		return this ;
	}

	
}

