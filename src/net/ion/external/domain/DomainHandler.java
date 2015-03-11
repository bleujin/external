package net.ion.external.domain;

import java.util.Iterator;

public interface DomainHandler<T> {

	public T handle(Iterator<Domain> domains) ;
}
