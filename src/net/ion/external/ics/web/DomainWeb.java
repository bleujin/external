package net.ion.external.ics.web;

import java.io.File;
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

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.*;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.craken.tree.Fqn;
import net.ion.external.domain.Domain;
import net.ion.external.domain.DomainHandler;
import net.ion.external.domain.DomainInfoHandler;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.util.WebUtil;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.util.FileUtil;
import net.ion.radon.core.ContextParam;

@Path("/domain")
public class DomainWeb implements Webapp {

	private DomainSub dsub;
	public DomainWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry){
		this.dsub = dentry.dsub() ;
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
	
	@Path("/{did}/scat/{catid}")
	@POST
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public String addSiteCategory(@PathParam("did") final String did, @PathParam("catid") final String catId, @DefaultValue("false") @FormParam("includeSub") boolean includeSub) {
		domain(did).addSiteCategory(catId, includeSub) ;
		return catId + " created" ;
	}
	
	@Path("/{did}/gcat/{catid}")
	@POST
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public String addGalleryCategory(@PathParam("did") final String did, @PathParam("catid") final String catId, @DefaultValue("false") @FormParam("includeSub") boolean includeSub) {
		domain(did).addGalleryCategory(catId, includeSub) ;
		return catId + " created" ;
	}

    // templates
    @GET
    @Path("/{did}/article/template")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject viewTemplate(@PathParam("did") final String did) throws IOException {

        JsonObject result = new JsonObject();
        ReadSession session = dsub.craken().login();

        result.put("info", session.ghostBy("/menus/domain").property("article").asString());
        result.put("samples", WebUtil.findArticleTemplates()) ;
        result.put("template", session.ghostBy(fqnBy(did, "/article/template")).property("template").asString());
        return result;
    }

    @POST
    @Path("/{did}/article/template")
    public String editTemplate(@PathParam("did") final String did, @FormParam("template") final String template) throws IOException {
        dsub.craken().login().tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                WriteNode found = wsession.pathBy(fqnBy(did, "/article/template"));
                FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, did + ".searcher.template.bak"), found.property("template").asString());

                found.property("template", template);
                return null;
            }
        });
        return "modified template : " + did;
    }

    private Fqn fqnBy(String did, String rest) {
        return Fqn.fromString("/domain/" + did + rest);
    }

    @GET
    @Path("/{did}/article/sampletemplate/{filename}")
    @Produces(ExtMediaType.TEXT_PLAIN_UTF8)
    public String viewArticleSampleTemplate(@PathParam("did") String did, @PathParam("filename") String fileName) throws IOException {
        return WebUtil.viewArticleTemplate(fileName) ;
    }


	private Domain domain(String did){
		return dsub.findDomain(did) ;
	}
}
