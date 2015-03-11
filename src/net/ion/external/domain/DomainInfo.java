package net.ion.external.domain;

import java.io.IOException;


public class DomainInfo {

	private Domain domain;
	DomainInfo(Domain domain){
		this.domain = domain ;
	}
	
	
	public <T> T siteCategory(DomainInfoHandler<T> nodeInfo) throws IOException{
		return nodeInfo.handle(domain.session().ghostBy("/domain", domain.getId(), "scat").children()) ;
	}
	
	public <T> T galleryCategory(DomainInfoHandler<T> nodeInfo) throws IOException{
		return nodeInfo.handle(domain.session().ghostBy("/domain", domain.getId(), "gcat").children()) ;
	}
	
	

}