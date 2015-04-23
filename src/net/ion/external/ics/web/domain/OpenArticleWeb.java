package net.ion.external.ics.web.domain;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.radon.core.ContextParam;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.plugins.providers.UncertainOutput;
import org.jboss.resteasy.spi.HttpRequest;

@Path("/article")
public class OpenArticleWeb implements Webapp{

	private ArticleWeb rweb;
	public OpenArticleWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry, @ContextParam(QueryTemplateEngine.EntryName) QueryTemplateEngine qengine) throws IOException {
		this.rweb = new ArticleWeb(dentry, qengine) ;
	}
	
	@GET
	@Path("/{did}/query.json")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput jquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {

		return rweb.jquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.xml")
	@Produces(ExtMediaType.APPLICATION_XML_UTF8)
	public StreamingOutput xquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {

		return rweb.xquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.csv")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public StreamingOutput cquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {
		return rweb.cquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.template")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public UncertainOutput tquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request, @DefaultValue("false") @QueryParam("html") final boolean isHtml) throws IOException, ParseException {

		return rweb.tquery(did, query, sort, skip, offset, indent, debug, request, isHtml) ;
	}

	@GET
	@Path("/{did}/list")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject listArticle(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException, ParseException {
		return rweb.listArticle(did, query, offset) ;
	}

	@GET
	@Path("/{did}/view/{catid}/{artid}")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput viewArticle(@PathParam("did") String did, @PathParam("catid") String catid, @PathParam("artid") int artid) {
		return rweb.viewArticle(did, catid, artid) ;
	}
	@GET
	@Path("/{did}/thumbimg/{catid}/{artid}.stream")
	public UncertainOutput viewThumbImage(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId) {
		return rweb.viewThumbImage(did, catId, artId) ;
	}

	@GET
	@Path("/{did}/afield/{catid}/{artid}/{aid}.stream")
	public UncertainOutput viewAfieldResource(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId, @PathParam("aid") String afieldId) {
		return rweb.viewAfieldResource(did, catId, artId, afieldId) ;
	}

	@GET
	@Path("/{did}/content/{catid}/{artid}/{resourceid}.stream")
	public UncertainOutput viewContentImage(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId, @PathParam("resourceid") final String resourceId) {
		return rweb.viewContentImage(did, catId, artId, resourceId) ;
	}

	@GET
	@Path("/{did}/storypage/{catid}/{artid}")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject storyPage(@PathParam("did") String did, @PathParam("catid") String catid, @PathParam("artid") final int artid) throws IOException {
		return rweb.storyPage(did, catid, artid) ;
	}


}
