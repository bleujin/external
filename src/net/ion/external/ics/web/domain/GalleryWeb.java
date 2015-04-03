package net.ion.external.ics.web.domain;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;
import net.ion.external.domain.DomainSub;
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
import net.ion.framework.util.Debug;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.NumberUtil;
import net.ion.radon.core.ContextParam;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.plugins.providers.UncertainOutput;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import java.io.*;

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

    @GET
    @Path("/{did}/gallery")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject query() throws IOException {
        JsonObject result = new JsonObject();
        result.put("info", session.ghostBy("/menus/domain").property("gallery").asString());
        return result;
    }

    @GET
    @Path("/{did}/list")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject listGallery(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException{
        XIterable<GalleryX> gallerys = dsub.findDomain(did).datas().gallerys().where(query).offset(offset).find();;
        JsonObject result = JsonObject.create() ;
        JsonArray jarray = new JsonArray();
        result.put("result", jarray) ;

        for(GalleryX gallery : gallerys) {
            JsonArray rowArray = new JsonArray().add(new JsonPrimitive(gallery.galId())).add(new JsonPrimitive(gallery.catId())).add(new JsonPrimitive(gallery.asString(Def.Gallery.Subject)))
                    .add(new JsonPrimitive(gallery.asString(Def.Gallery.Width))).add(new JsonPrimitive(gallery.asString(Def.Gallery.Height))).add(new JsonPrimitive(gallery.asString(Def.Gallery.FileSize)))
                    .add(new JsonPrimitive(gallery.asString(Def.Gallery.ModDay))) ;
            jarray.add(rowArray) ;
        }
        return result ;
    }

    @GET
    @Path("/{did}/view/{galid}")
    public UncertainOutput viewImage(@PathParam("did") final String did, @PathParam("galid") final int galid) throws IOException{

    	final GalleryX gallery = dsub.findDomain(did).datas().findGallery(galid) ;
    	if (! gallery.exists()) throw new WebApplicationException(404) ;
    	
        return new UncertainOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream input = gallery.dataStream() ;
                try {
                    IOUtil.copy(input, output);
                } finally {
                    IOUtil.close(input);
                }
            }

            @Override
            public MediaType getMediaType() {
                return ExtMediaType.guessImageType(gallery.typeCd()) ;
            }
        } ;
    }

    
    @GET
    @Path("/{did}/crop/{galid}")
    public UncertainOutput crop(@PathParam("did") String did, @PathParam("galid") int galid, 
    		final @DefaultValue("0") @QueryParam("x") int x, 
    		final @DefaultValue("0") @QueryParam("y") int y, 
    		final @DefaultValue("100") @QueryParam("width") int width, 
    		final @DefaultValue("100") @QueryParam("height") int height) throws IOException {
    	final GalleryX gallery = dsub.findDomain(did).datas().findGallery(galid) ;
    	if (! gallery.exists()) throw new WebApplicationException(404) ;

        return new UncertainOutput() {
			public MediaType getMediaType() {
                return ExtMediaType.guessImageType(gallery.typeCd()) ;
			}
			public void write(OutputStream output) throws IOException, WebApplicationException {
				InputStream input = gallery.cropWith(x, y, width, height) ;
                try {
                    IOUtil.copy(input, output);
                } finally {
                    IOUtil.close(input);
                }
			}
        } ;
    }

    @GET
    @Path("/{did}/resize/{galid}")
    public UncertainOutput resize(@PathParam("did") String did, @PathParam("galid") int galid, 
    		final @DefaultValue("100") @QueryParam("width") int width, 
    		final @DefaultValue("100") @QueryParam("height") int height) throws IOException {
    	final GalleryX gallery = dsub.findDomain(did).datas().findGallery(galid) ;
    	if (! gallery.exists()) throw new WebApplicationException(404) ;

        return new UncertainOutput() {
			public MediaType getMediaType() {
                return ExtMediaType.guessImageType(gallery.typeCd()) ;
			}
			public void write(OutputStream output) throws IOException, WebApplicationException {
				InputStream input = gallery.resizeWith(width, height) ;
                try {
                    IOUtil.copy(input, output);
                } finally {
                    IOUtil.close(input);
                }
			}
        } ;
    }

    
    
    
    
    
    
	// query
	@GET
	@Path("/{did}/query.json")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput jquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
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

	private GalleryChildrenX findGallery(String did, String query, String sort, String skip, String offset, HttpRequest request, MultivaluedMap<String, String> map) throws IOException, ParseException {
		if (request.getHttpMethod().equalsIgnoreCase("POST") && request.getDecodedFormParameters().size() > 0)
			map.putAll(request.getDecodedFormParameters());

		return dsub.findDomain(did).datas().gallerys().where(query).sort(sort).skip(NumberUtil.toInt(skip, 0)).offset(NumberUtil.toInt(offset, 10));
	}

	@GET
	@Path("/{did}/query.xml")
	@Produces(ExtMediaType.APPLICATION_XML_UTF8)
	public StreamingOutput xquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
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
	public StreamingOutput cquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
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
	public String tquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
			@QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

		try {
			MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
			final XIterable<GalleryX> gallerys = findGallery(did, query, sort, skip, offset, request, map).find();

			StringWriter writer = new StringWriter();
			String resourceName = "/domain/"+ did + "/gallery" + ".template" ;
			qengine.merge(resourceName, MapUtil.<String, Object> chainMap().put("gallerys", gallerys).put("params", map).toMap(), writer);
			
			return writer.toString() ;
		} catch (org.apache.velocity.exception.ParseErrorException tex) {
			tex.printStackTrace(); 
			return tex.getMessage();
		}
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
		result.put("template", session.ghostBy(fqnBy(did, "/gallery")).property("template").asString());
		return result;
	}

	@POST
	@Path("/{did}/template")
	public String editGalleryTemplate(@PathParam("did") final String did, @FormParam("template") final String template) throws IOException {
		dsub.craken().login().tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode found = wsession.pathBy(fqnBy(did, "/gallery"));
				FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, did + ".gallery.template.bak"), found.property("template").asString());

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
