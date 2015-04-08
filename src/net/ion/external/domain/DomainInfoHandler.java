package net.ion.external.domain;

import java.util.Iterator;

public interface DomainInfoHandler<T> {

	public T handle(Iterator<Domain> domains) ;
}
