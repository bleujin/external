package net.ion.external.ics.web;

import javax.ws.rs.Path;

import net.ion.external.domain.Domain;
import net.ion.external.domain.DomainSub;
import net.ion.radon.core.ContextParam;

@Path("/{did}/gallery")
public class GalleryWeb implements WebApp {

	private DomainSub dsub ;
	public GalleryWeb(@ContextParam("dsub") DomainSub dsub){
		this.dsub = dsub ;
	}
	

	
	
	
	private Domain findDomain(String did){
		return dsub.findDomain(did) ;
	}
	
}
