package net.ion.external.ics.web.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.plugins.providers.UncertainOutput;
import org.jboss.resteasy.spi.HttpRequest;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.GalleryChildrenX;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.XIterable;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.util.WebUtil;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.NumberUtil;
import net.ion.radon.core.ContextParam;

@Path("/gallery")
public class OpenGalleryWeb implements Webapp {
	private GalleryWeb rweb;

	public OpenGalleryWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry, @ContextParam(QueryTemplateEngine.EntryName) QueryTemplateEngine qengine) throws IOException {
		this.rweb = new GalleryWeb(dentry, qengine) ;
	}

	@GET
	@Path("/{did}/list")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject listGallery(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException, ParseException {
		return rweb.listGallery(did, query, offset) ;
	}

	@GET
	@Path("/{did}/view/{galid}")
	public UncertainOutput viewImage(@PathParam("did") final String did, @PathParam("galid") final int galid) throws IOException {
		return rweb.viewImage(did, galid) ;
	}

	@GET
	@Path("/{did}/crop/{galid}")
	public UncertainOutput crop(@PathParam("did") String did, @PathParam("galid") int galid, final @DefaultValue("0") @QueryParam("x") int x, final @DefaultValue("0") @QueryParam("y") int y, final @DefaultValue("100") @QueryParam("width") int width,
			final @DefaultValue("100") @QueryParam("height") int height) throws IOException {
		return rweb.crop(did, galid, x, y, width, height) ;
	}

	@GET
	@Path("/{did}/resize/{galid}")
	public UncertainOutput resize(@PathParam("did") String did, @PathParam("galid") int galid, final @DefaultValue("100") @QueryParam("width") int width, final @DefaultValue("100") @QueryParam("height") int height) throws IOException {
		return rweb.resize(did, galid, width, height, null) ;
	}

	// query
	@GET
	@Path("/{did}/query.json")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput jquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) throws IOException {

		return rweb.jquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.xml")
	@Produces(ExtMediaType.APPLICATION_XML_UTF8)
	public StreamingOutput xquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) throws IOException {

		return rweb.xquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.csv")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public StreamingOutput cquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
			@QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		return rweb.cquery(did, query, sort, skip, offset, indent, debug, request) ;
	}

	@GET
	@Path("/{did}/query.template")
	public UncertainOutput tquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request, @DefaultValue("false") @QueryParam("html") final boolean isHtml) throws IOException {

		return rweb.tquery(did, query, sort, skip, offset, indent, debug, request, isHtml) ;

	}

}
