package net.ion.external.domain;

import java.io.IOException;

import net.ion.craken.Craken;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;

public class DomainSub {

	private ReadSession session;
	private Craken ic ;
	
	public DomainSub(Craken ic) throws IOException {
		this.ic = ic ;
		this.session = ic.login() ;
	}

	public static DomainSub create(Craken ic) throws IOException {
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
		return Domain.by(ic, session.pathBy("/domain/" + did)) ;
	}

	public boolean existDomain(String did) {
		return ! session.pathBy("/domain/" + did).isGhost();
	}
	

}
