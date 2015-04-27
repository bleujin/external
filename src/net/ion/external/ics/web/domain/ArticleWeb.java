package net.ion.external.ics.web.domain;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.external.domain.Domain;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.*;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.util.WebUtil;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.util.*;
import net.ion.radon.core.ContextParam;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.plugins.providers.UncertainOutput;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

@Path("/article")
public class ArticleWeb implements Webapp {

	private DomainSub dsub;
	private ReadSession session;
	private QueryTemplateEngine qengine;

	public ArticleWeb(@ContextParam(DomainEntry.EntryName) DomainEntry dentry, @ContextParam(QueryTemplateEngine.EntryName) QueryTemplateEngine qengine) throws IOException {
		this.dsub = dentry.dsub();
		this.session = dsub.craken().login();
		this.qengine = qengine;
	}

	// query
	@GET
	@Path("/{did}/article")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject query() throws IOException {
		JsonObject result = new JsonObject();
		result.put("info", session.ghostBy("/menus/domain").property("article").asString());
		return result;
	}

	@GET
	@Path("/{did}/query.json")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput jquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {

		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				try {
					MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
					final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();
					OutputHandler ohandler = OutputHandler.createJson(writer, indent);
					ohandler.out(articles, new JsonObject(), new JsonObject());
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
					writer.write(e.getMessage());
				} catch(net.ion.rosetta.error.ParserException ex){
					ex.printStackTrace();
					writer.write(ex.getMessage());
				} finally {
					writer.flush();
				}
			}
		};
	}

	private ArticleChildrenX findArticle(String did, String query, String sort, String skip, String offset, HttpRequest request, MultivaluedMap<String, String> map) throws IOException {
		if (request.getHttpMethod().equalsIgnoreCase("POST") && request.getDecodedFormParameters().size() > 0)
			map.putAll(request.getDecodedFormParameters());

		return dsub.findDomain(did).datas().articles().where(query).sort(sort).skip(NumberUtil.toInt(skip, 0)).offset(NumberUtil.toInt(offset, 10));
	}

	@GET
	@Path("/{did}/query.xml")
	@Produces(ExtMediaType.APPLICATION_XML_UTF8)
	public StreamingOutput xquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {

		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				try {
					MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
					final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();
					OutputHandler ohandler = OutputHandler.createXml(writer, indent);
					ohandler.out(articles, new JsonObject(), new JsonObject());
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
					writer.write(e.getMessage());
				} catch(net.ion.rosetta.error.ParserException ex){
					ex.printStackTrace();
					writer.write(ex.getMessage());
				} finally {
					writer.flush();
				}

			}
		};
	}

	@GET
	@Path("/{did}/query.csv")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public StreamingOutput cquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request) {

		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
				try {
					final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();
					OutputHandler ohandler = OutputHandler.createCsv(writer);
					ohandler.out(articles, new JsonObject(), new JsonObject());
				} catch (IOException e) {
					e.printStackTrace();
					writer.write(e.getMessage());
				} catch(net.ion.rosetta.error.ParserException ex){
					ex.printStackTrace();
					writer.write(ex.getMessage());
				} finally {
					writer.flush();
				}
			}
		};
	}

	@GET
	@Path("/{did}/query.template")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public UncertainOutput tquery(@PathParam("did") final String did, @DefaultValue("") @QueryParam("query") final String query, @DefaultValue("") @QueryParam("sort") final String sort, @DefaultValue("0") @QueryParam("skip") final String skip,
			@DefaultValue("10") @QueryParam("offset") final String offset, @QueryParam("indent") boolean indent, @QueryParam("debug") boolean debug, @Context final HttpRequest request, @DefaultValue("false") @QueryParam("html") final boolean isHtml) throws IOException, ParseException {

		return new UncertainOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				try {
					MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
					final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();

					String resourceName = "/domain/" + did + "/article" + ".template";
					qengine.merge(resourceName, MapUtil.<String, Object> chainMap().put("articles", articles).put("params", map).put("session", session).toMap(), writer);

				} catch (org.apache.velocity.exception.ParseErrorException ex) {
					ex.printStackTrace();
					writer.write(ex.getMessage());
				} catch(net.ion.rosetta.error.ParserException ex){
					ex.printStackTrace();
					writer.write(ex.getMessage());
				} finally {
					writer.flush();
				}
			}

			@Override
			public MediaType getMediaType() {
				return isHtml ? MediaType.valueOf(ExtMediaType.TEXT_HTML_UTF8.toString()) : MediaType.valueOf(ExtMediaType.TEXT_PLAIN_UTF8.toString());
			}
		};

	}

	@GET
	@Path("/{did}/list")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject listArticle(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException, ParseException {
		XIterable<ArticleX> articles = dsub.findDomain(did).datas().articles().query(query).offset(offset).find();

		JsonObject result = JsonObject.create();
		JsonArray jarray = new JsonArray();
		result.put("result", jarray);

		for (ArticleX article : articles) {
			JsonArray rowArray = new JsonArray().add(new JsonPrimitive(article.artId())).add(new JsonPrimitive(article.catId())).add(new JsonPrimitive(article.asString(Def.Article.Subject))).add(new JsonPrimitive(article.asString(Def.Article.ModDay)));
			jarray.add(rowArray);
		}
		return result;
	}

	@GET
	@Path("/{did}/view/{catid}/{artid}")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public StreamingOutput viewArticle(@PathParam("did") String did, @PathParam("catid") String catid, @PathParam("artid") int artid) {
		final ArticleX article = dsub.findDomain(did).datas().article(catid, artid);
		// return JsonObject.create().put("subject", article.asString("subject")).put("content", article.asString("content"));
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output);
				article.jsonWrite(writer);
				writer.flush();
			}
		};
	}

	private MediaType guessFromFileName(String fileName) {
		return ExtMediaType.guessImageType(StringUtil.substringAfterLast(fileName, "."));
	}

	@GET
	@Path("/{did}/thumbimg/{catid}/{artid}.stream")
	public UncertainOutput viewThumbImage(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId) {
		final ArticleX article = dsub.findDomain(did).datas().article(catId, artId);

		return new UncertainOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				IOUtil.copyNCloseSilent(article.thumbnailStream(), output);
			}

			@Override
			public MediaType getMediaType() {
				return guessFromFileName(article.asString("thumbimg"));
			}
		};
	}

	@GET
	@Path("/{did}/afield/{catid}/{artid}/{aid}.stream")
	public UncertainOutput viewAfieldResource(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId, @PathParam("aid") final String afieldId) {
		final AfieldValueX avalue = dsub.findDomain(did).datas().article(catId, artId).asAfield(afieldId);

		return new UncertainOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				IOUtil.copy(avalue.dataStream(), output);
			}

			@Override
			public MediaType getMediaType() {
				String fileName = avalue.asString(afieldId);
				return guessFromFileName(fileName);
			}
		};
	}

    @GET
    @Path("/{did}/afield/{catid}/{artid}/{aid}.download")
    public Response downloadAfieldResource(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId, @PathParam("aid") final String afieldId) throws IOException {
        final AfieldValueX avalue = dsub.findDomain(did).datas().article(catId, artId).asAfield(afieldId);

        if(!"File".equals(avalue.typeCd()) && !"Image".equals(avalue.typeCd())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        String fileName = StringUtil.substringAfterLast(avalue.asString("stringvalue"), "/");
        Response.ResponseBuilder respBuilder = Response.ok(avalue.dataStream());
        respBuilder.type(guessFromFileName(fileName)) ;
        respBuilder.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"") ;

        return respBuilder.build() ;
    }

	@GET
	@Path("/{did}/content/{catid}/{artid}/{resourceid}.stream")
	public UncertainOutput viewContentImage(@PathParam("did") String did, @PathParam("catid") String catId, @PathParam("artid") int artId, @PathParam("resourceid") final String resourceId) {
		final ArticleX article = dsub.findDomain(did).datas().article(catId, artId);

		return new UncertainOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				IOUtil.copyNCloseSilent(article.asStream(resourceId), output);
			}

			@Override
			public MediaType getMediaType() {
				return ExtMediaType.APPLICATION_OCTET_STREAM_TYPE;
			}
		};
	}

	@GET
	@Path("/{did}/storypage/{catid}/{artid}")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject storyPage(@PathParam("did") String did, @PathParam("catid") String catid, @PathParam("artid") final int artid) throws IOException {
		XIterable<TemplateX> stemplates = findDomain(did).datas().templates().selectStory().category(catid).find();

		Iterator<TemplateX> iter = stemplates.iterator();
		JsonObject result = JsonObject.create();

		JsonArray nodes = new JsonArray();
		int seq = 1;
		while (iter.hasNext()) {
			TemplateX template = iter.next();
			nodes.add(new JsonObject().put("tplid", template.tplId()).put("name", template.asString("name")).put("form", seq++).put("link", template.fileName(artid)));
		}

		return result.put("nodes", nodes);
	}

	private Domain findDomain(String did) {
		return dsub.findDomain(did);
	}

	// templates
	@GET
	@Path("/{did}/template")
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject viewTemplate(@PathParam("did") final String did) throws IOException {

		JsonObject result = new JsonObject();

		result.put("info", session.ghostBy("/menus/domain").property("article").asString());
		result.put("samples", WebUtil.findArticleTemplates());
		result.put("template", session.ghostBy("/domain", did, "article").property("template").asString());
		return result;
	}

	@POST
	@Path("/{did}/template")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String editTemplate(@PathParam("did") final String did, @FormParam("template") final String template) throws IOException {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode found = wsession.pathBy("/domain", did, "article");
				FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, did + ".article.template.bak"), found.property("template").asString());

				found.property("template", template);
				return null;
			}
		});
		return "modified article template : " + did;
	}

	@GET
	@Path("/{did}/samples/{filename}")
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String viewSampleTemplate(@PathParam("did") String did, @PathParam("filename") String fileName) throws IOException {
		return WebUtil.viewArticleTemplate(fileName);
	}

}
