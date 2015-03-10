package net.ion.external.domain;

import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Function;

import net.ion.craken.ICSCraken;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.ReadChildren;

public class DomainSub {

	private ReadSession session;
	private ICSCraken craken ;
	
	public DomainSub(ICSCraken craken) throws IOException {
		this.craken = craken ;
		this.session = craken.login() ;
	}

	
	public ICSCraken craken(){
		return craken ;
	}
	
	
	public static DomainSub create(ICSCraken ic) throws IOException {
		return new DomainSub(ic);
	}

	
	public void createDomain(final String did) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/" + did).property("did", did) ;
				return null;
			}
		}) ;
	}

	public Domain findDomain(final String did){
		return Domain.by(craken, session.ghostBy("/domain/" + did)) ;
	}

	public boolean existDomain(String did) {
		return ! session.pathBy("/domain/" + did).isGhost();
	}


	public <T> T domains(DomainHandler<T> dhandler) {
		final IteratorList<ReadNode> citer = session.ghostBy("/domain").children().iterator() ;
		
		return dhandler.handle(new Iterator<Domain>(){
			@Override
			public boolean hasNext() {
				return citer.hasNext();
			}
			@Override
			public Domain next() {
				return Domain.by(craken, citer.next());
			}
			@Override
			public void remove() {
			}
		}) ;
	}
	

}
