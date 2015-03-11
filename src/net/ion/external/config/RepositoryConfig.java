package net.ion.external.config;


public class RepositoryConfig {

	private String crakenConfig = "./resource/config/craken-local-config.xml" ;
	private String adminHomeDir ;
	private String wsName ;
	
	public RepositoryConfig(String crakenConfig, String adminHomeDir, String wsName) { 
		this.crakenConfig = crakenConfig ;
		this.adminHomeDir = adminHomeDir ;
		this.wsName = wsName ;
	}

	
	public String adminHomeDir(){
		return adminHomeDir ;
	}
	
	public String crakenConfig(){
		return crakenConfig ;
	}
	
	public String wsName(){
		return wsName ;
	}

}
