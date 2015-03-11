package net.ion.craken;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;


public class ICSCraken {

    public static final String EntryName = "craken";
    private RepositoryImpl repository;
	private String wsName;
	private ICSCraken(RepositoryImpl repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
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

