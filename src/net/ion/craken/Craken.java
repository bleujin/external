package net.ion.craken;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;


public class Craken {

    public static final String EntryName = "craken";
    private RepositoryImpl repository;
	private String wsName;
	private Craken(RepositoryImpl repository, String wsName) {
		this.repository = repository ;
		this.wsName = wsName ;
	}

	public static Craken create() throws IOException{
		RepositoryImpl r = RepositoryImpl.create();
		r.createWorkspace("ics", WorkspaceConfigBuilder.directory("./resource/ics")) ;
		
		return new Craken(r, "ics") ;
	}
	
	public static Craken test() throws CorruptIndexException, IOException{
		return new Craken(RepositoryImpl.inmemoryCreateWithTest(), "test") ;
	}
	
	public ReadSession login() throws IOException{
		return repository.login(this.wsName) ;
	}
	
	public Craken stop(){
		repository.shutdown() ;
		return this ;
	}
	
}

