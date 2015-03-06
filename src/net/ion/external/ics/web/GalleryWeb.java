package net.ion.external.ics.web;

import net.ion.external.domain.DomainSub;
import net.ion.radon.core.ContextParam;

public class GalleryWeb implements WebApp {

	private DomainSub dsub ;
	public GalleryWeb(@ContextParam("dsub") DomainSub dsub){
		this.dsub = dsub ;
	}
	
	
	
	
}
