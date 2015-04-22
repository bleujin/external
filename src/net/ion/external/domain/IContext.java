package net.ion.external.domain;


public interface IContext {

	public <T> T getAttribute(Class<T> clz) ;
	public <T> T callAttribute(Class<T> clz) ;
}
