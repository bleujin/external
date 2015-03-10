package net.ion.external.ics.web;

import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.external.domain.Domain;
import net.ion.external.domain.DomainHandler;
import net.ion.external.domain.DomainInfoHandler;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.radon.core.ContextParam;

@Path("/domain")
public class DomainWeb implements Webapp {

	private DomainSub dsub;
	public DomainWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry){
		this.dsub = dentry.dsub() ;
	}
	

	
	@Path("/{did}/create")
	@POST
	public String create(@PathParam("did") String did) {
		dsub.createDomain(did);
		return did + " created" ;
	}
	
	@Path("/list")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonArray list() {
		return dsub.domains(new DomainHandler<JsonArray>() {
			@Override
			public JsonArray handle(Iterator<Domain> domains) {
				JsonArray result = new JsonArray() ;
				while(domains.hasNext()) {
					result.add(JsonPrimitive.create(domains.next().getId())) ;
				}
				return result;
			}
		}) ;
	}
	
	
	@Path("/{did}/info")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject info(@PathParam("did") final String did) throws IOException{
		JsonObject result = JsonObject.create() ;
		
		JsonArray scats = domain(did).info().siteCategory(new DomainInfoHandler<JsonArray>() {
			@Override
			public JsonArray handle(ReadChildren children) {
				JsonArray result = new JsonArray() ;
				for (ReadNode node : children.iterator()) {
					result.add(new JsonObject().put("catid", node.fqn().name()).put("includesub", node.property("includesub").asBoolean()).put("catpath", node.session().ghostBy("/datas/scat", node.fqn().name()).property("catpath").asString())) ;
				}
				return result;
			}
		}) ;
		
		JsonArray gcats = domain(did).info().galleryCategory(new DomainInfoHandler<JsonArray>() {
			@Override
			public JsonArray handle(ReadChildren children) {
				JsonArray result = new JsonArray() ;
				for(ReadNode node : children.iterator()) {
					result.add(new JsonObject().put("catid", node.fqn().name()).put("includesub", node.property("includesub").asBoolean()).put("catpath", node.session().ghostBy("/datas/gcat", node.fqn().name()).property("catpath").asString())) ;
				}
				return result;
			}
		});
		
		result.put("scats", scats) ;
		result.put("gcats", gcats) ;
		
		return result ;
	}
	
	@Path("/{did}/scat/{catid}/create")
	@POST
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public String addSiteCategory(@PathParam("did") final String did, @PathParam("catid") final String catId, @DefaultValue("F") @FormParam("includeSub") String includeSub) {
		domain(did).addSiteCategory(catId, "T".equals(includeSub) ? true : false) ;
		return catId + " created" ;
	}
	
	@Path("/{did}/gcat/{catid}/create")
	@POST
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public String addGalleryCategory(@PathParam("did") final String did, @PathParam("catid") final String catId, @DefaultValue("F") @FormParam("includeSub") String includeSub) {
		domain(did).addGalleryCategory(catId, "T".equals(includeSub) ? true : false) ;
		return catId + " created" ;
	}

	private Domain domain(String did){
		return dsub.findDomain(did) ;
	}
}
