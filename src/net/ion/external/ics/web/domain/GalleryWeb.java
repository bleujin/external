package net.ion.external.ics.web.domain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.spi.HttpRequest;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.GalleryChildrenX;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.XIterable;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.util.WebUtil;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.MapUtil;
import net.ion.radon.core.ContextParam;

@Path("/gallery")
public class GalleryWeb {

	private DomainSub dsub;
	private ReadSession session;
	private QueryTemplateEngine qengine;

	public GalleryWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry, @ContextParam(QueryTemplateEngine.EntryName) QueryTemplateEngine qengine) throws IOException {
		this.dsub = dentry.dsub();
		this.session = dsub.craken().login();
		this.qengine = qengine ;
	}
	
	
	

	// query
	@GET
	@Path("/{did}/query.json")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput jquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") int skip, @DefaultValue("10") @QueryParam("offset") int offset,
			@QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
		final XIterable<GalleryX> gallerys = findGallery(did, query, sort, skip, offset, request, map).find();
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				OutputHandler ohandler = OutputHandler.createJson(writer, indent) ;
				ohandler.out(gallerys, new JsonObject(), new JsonObject()) ;
				writer.flush(); 
			}
		};
	}

	private GalleryChildrenX findGallery(String did, String query, String sort, int skip, int offset, HttpRequest request, MultivaluedMap<String, String> map) throws IOException, ParseException {
		if (request.getHttpMethod().equalsIgnoreCase("POST") && request.getDecodedFormParameters().size() > 0)
			map.putAll(request.getDecodedFormParameters());

		return dsub.findDomain(did).datas().gallerys().where(query).sort(sort).skip(skip).offset(offset) ;
	}

	@GET
	@Path("/{did}/query.xml")
	@Produces(ExtMediaType.APPLICATION_XML_UTF8)
	public StreamingOutput xquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") int skip, @DefaultValue("10") @QueryParam("offset") int offset,
			@QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
		final XIterable<GalleryX> gallerys = findGallery(did, query, sort, skip, offset, request, map).find();
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				OutputHandler ohandler = OutputHandler.createXml(writer, indent) ;
				ohandler.out(gallerys, new JsonObject(), new JsonObject()) ;
				writer.flush(); 
				
			}
		};
	}

	@GET
	@Path("/{did}/query.csv")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public StreamingOutput cquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") int skip, @DefaultValue("10") @QueryParam("offset") int offset,
			@QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
		final XIterable<GalleryX> gallerys = findGallery(did, query, sort, skip, offset, request, map).find();
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				OutputHandler ohandler = OutputHandler.createCsv(writer) ;
				ohandler.out(gallerys, new JsonObject(), new JsonObject()) ;
				writer.flush(); 
			}
		};
	}

	@GET
	@Path("/{did}/query.template")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String tquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") int skip, @DefaultValue("10") @QueryParam("offset") int offset,
			@QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		try {
			MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
			final XIterable<GalleryX> gallerys = findGallery(did, query, sort, skip, offset, request, map).find();

			StringWriter writer = new StringWriter();
			String resourceName = "/domain/"+ did + "/article" + ".template" ;
			qengine.merge(resourceName, MapUtil.<String, Object> chainMap().put("articles", gallerys).put("params", map).toMap(), writer);
			
			return writer.toString() ;
		} catch (org.apache.velocity.exception.ParseErrorException tex) {
			tex.printStackTrace(); 
			return tex.getMessage();
		}
	}	
	
	
	@GET
	@Path("/{did}/list")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8) 
	public StreamingOutput listGallery(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException{
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				GalleryChildrenX gallerys = dsub.findDomain(did).datas().gallerys().where(query).offset(offset) ;
				JsonWriter jwriter = new JsonWriter(new OutputStreamWriter(output)) ;
				
				jwriter.beginObject().name("result").beginArray() ;
				gallerys.find().jsonSelf(jwriter, Def.Gallery.GalId, Def.Gallery.CatId, Def.Gallery.Subject, Def.Gallery.Width);
				jwriter.endArray().endObject() ;
				jwriter.flush();
			}
		};
	}
	
	
	
	
	
	
	// template
	
	@GET
	@Path("/{did}/template")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject viewGalleryTemplate(@PathParam("did") final String did) throws IOException {

		JsonObject result = new JsonObject();
		ReadSession session = dsub.craken().login();

		result.put("info", session.ghostBy("/menus/domain").property("gallery").asString());
		result.put("samples", WebUtil.findGalleryTemplates());
		result.put("template", session.ghostBy(fqnBy(did, "/gallery/template")).property("template").asString());
		return result;
	}

	@POST
	@Path("/{did}/template")
	public String editGalleryTemplate(@PathParam("did") final String did, @FormParam("template") final String template) throws IOException {
		dsub.craken().login().tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode found = wsession.pathBy(fqnBy(did, "/gallery/template"));
				FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, did + ".article.template.bak"), found.property("template").asString());

				found.property("template", template);
				return null;
			}
		});
		return "modified gallery template : " + did;
	}

	private Fqn fqnBy(String did, String rest) {
		return Fqn.fromString("/domain/" + did + rest);
	}

	@GET
	@Path("/{did}/samples/{filename}")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String viewGallerySampleTemplate(@PathParam("did") String did, @PathParam("filename") String fileName) throws IOException {
		return WebUtil.viewGalleryTemplate(fileName);
	}

}
