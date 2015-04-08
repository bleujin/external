package net.ion.external.domain;

import net.ion.craken.node.crud.ReadChildren;

public interface DomainNodeInfoHandler<T> {
	
	public T handle(ReadChildren children)  ;

}
