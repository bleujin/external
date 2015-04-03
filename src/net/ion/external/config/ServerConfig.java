package net.ion.external.config;

import org.infinispan.configuration.cache.CacheMode;


public class ServerConfig {

	private String id = "niss";
	private int port = 9000 ;
	private String password;
	private String clusterName ;
	private CacheMode cacheMode;

	public ServerConfig(String id, int port, String password, String clusterName, String cacheMode) {
		this.id = id ;
		this.port = port ;
		this.password = password ;
		this.clusterName = clusterName ;
		this.cacheMode = CacheMode.valueOf(cacheMode) ;
	}
	
	public int port(){
		return port;
	}
	
	public String id() {
		return id ;
	}

	public String password(){
		return password ;
	}

	public String clusterName() {
		return clusterName;
	}

	public CacheMode cacheMode(){
		return cacheMode ;
	}
}
