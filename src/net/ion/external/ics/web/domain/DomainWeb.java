package net.ion.external.ics.web.domain;

import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.external.domain.Domain;
import net.ion.external.domain.Domain.Target;
import net.ion.external.domain.DomainData;
import net.ion.external.domain.DomainHandler;
import net.ion.external.domain.DomainInfoHandler;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.radon.core.ContextParam;

@Path("/domain")
public class DomainWeb implements Webapp {

	private DomainSub dsub;
	private ReadSession session;
	public DomainWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry) throws IOException{
		this.dsub = dentry.dsub() ;
		this.session = dsub.craken().login() ;
	}
	

	
	@Path("/{did}")
	@POST
	public String createDomain(@PathParam("did") String did) {
		dsub.createDomain(did);
		return did + " created" ;
	}

	@Path("/{did}")
	@DELETE
	public String removeDomain(@PathParam("did") String did) {
		dsub.removeDomain(did);
		return did + " removed" ;
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
	
	
	@Path("/{did}/overview")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject overview(@PathParam("did") final String did) throws IOException{
		JsonObject result = JsonObject.create() ;
		DomainData ddata = domain(did).datas() ;
		result.put("info", session.ghostBy("/menus/domain").property("overview").asString());
		result.put("status", new JsonObject()
				.put("Site Category", ddata.scategorys().find().count())
				.put("Gallery Category", ddata.gcategorys().find().count())
				.put("Articles", ddata.articles().find().count())
				.put("Galleries", ddata.gallerys().find().count())) ;
		
		return result;
	}
	
	
	
	
	// define
	
	@Path("/{did}/define")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject define(@PathParam("did") final String did) throws IOException{
		JsonObject result = JsonObject.create() ;
		
		JsonArray scats = domain(did).info().siteCategory(new DomainInfoHandler<JsonArray>() {
			@Override
			public JsonArray handle(ReadChildren children) {
				JsonArray result = new JsonArray() ;
				for (ReadNode node : children.iterator()) {
					result.add(new JsonObject().put("catid", node.fqn().name()).put("includesub", node.property("includesub").asBoolean())) ;
				}
				return result;
			}
		}) ;
		
		JsonArray gcats = domain(did).info().galleryCategory(new DomainInfoHandler<JsonArray>() {
			@Override
			public JsonArray handle(ReadChildren children) {
				JsonArray result = new JsonArray() ;
				for(ReadNode node : children.iterator()) {
					result.add(new JsonObject().put("catid", node.fqn().name()).put("includesub", node.property("includesub").asBoolean())) ;
				}
				return result;
			}
		});
		
		result.put("scats", scats) ;
		result.put("gcats", gcats) ;
		
		return result ;
	}
	
	
	@Path("/{did}/define")
	@POST
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String addCategory(@PathParam("did") final String did, @DefaultValue("scat") @FormParam("target") String target, @FormParam("catid") final String catId, @DefaultValue("false") @FormParam("includeSub") boolean includeSub) {
		domain(did).addCategory(Target.create(target), catId, includeSub) ;
		return catId + " created" ;
	}
	
	@Path("/{did}/define")
	@DELETE
	public String removeCategory(@PathParam("did") final String did, @DefaultValue("scat") @FormParam("target") String target, @FormParam("catid") final String catId) {
		domain(did).removeCategory(Target.create(target), catId) ;
		return catId + " removed" ;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

    
	private Domain domain(String did){
		return dsub.findDomain(did) ;
	}


}
