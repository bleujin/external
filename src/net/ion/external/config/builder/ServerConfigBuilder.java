package net.ion.external.config.builder;

import net.ion.external.config.ServerConfig;
import net.ion.framework.util.NumberUtil;
import net.ion.framework.util.StringUtil;

import org.infinispan.configuration.cache.CacheMode;
import org.w3c.dom.Node;

public class ServerConfigBuilder {

	private String id = "icses";
	private int port = 9001 ;
	private String password = "dkdldhs" ;
	private ConfigBuilder parent;
	private String clusterName;
	private String cacheMode;

	public ServerConfigBuilder(ConfigBuilder parent) {
		this.parent = parent ;
	}
	
	public ServerConfigBuilder node(Node node) {
		String id = node.getAttributes().getNamedItem("id").getTextContent();
		int port = NumberUtil.toInt(node.getAttributes().getNamedItem("port").getTextContent(), 9000) ;
		Node pnode = node.getAttributes().getNamedItem("password");
		String clusterName = node.getAttributes().getNamedItem("clusterName").getTextContent();
		String cacheMode = node.getAttributes().getNamedItem("cacheMode").getTextContent();
		return id(id).port(port).password(pnode).clusterName(clusterName).cacheMode(cacheMode) ;
	}
	
	private ServerConfigBuilder cacheMode(String cacheMode) {
		this.cacheMode = StringUtil.defaultIfEmpty(cacheMode, CacheMode.DIST_SYNC.toString()) ;
		return this;
	}

	private ServerConfigBuilder clusterName(String clusterName) {
		this.clusterName = StringUtil.defaultIfEmpty(clusterName, "ics6working") ;
		return this;
	}

	private ServerConfigBuilder password(Node pnode) {
		if (pnode != null) this.password = StringUtil.defaultIfEmpty(pnode.getTextContent(), "dkdldhs") ;
		return this;
	}

	public ServerConfigBuilder port(int port){
		this.port = port ;
		return this ;
	}
	
	public ServerConfigBuilder id(String id){
		this.id = StringUtil.defaultIfEmpty(id, "niss") ;
		return this ;
	}
	
	public ConfigBuilder parent(){
		return parent ;
	}

	public ServerConfig build() {
		return new ServerConfig(id, port, password, clusterName, cacheMode);
	}
}
