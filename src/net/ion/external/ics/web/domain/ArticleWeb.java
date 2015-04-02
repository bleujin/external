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
import net.ion.framework.util.FileUtil;
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
    public StreamingOutput jquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
                                  @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

        MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
        final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                OutputStreamWriter writer = new OutputStreamWriter(output);
                OutputHandler ohandler = OutputHandler.createJson(writer, indent);
                ohandler.out(articles, new JsonObject(), new JsonObject());
                writer.flush();
            }
        };
    }

    private ArticleChildrenX findArticle(String did, String query, String sort, String skip, String offset, HttpRequest request, MultivaluedMap<String, String> map) throws IOException, ParseException {
        if (request.getHttpMethod().equalsIgnoreCase("POST") && request.getDecodedFormParameters().size() > 0)
            map.putAll(request.getDecodedFormParameters());

        return dsub.findDomain(did).datas().articles().where(query).sort(sort).skip(NumberUtil.toInt(skip, 0)).offset(NumberUtil.toInt(offset, 10));
    }

    @GET
    @Path("/{did}/query.xml")
    @Produces(ExtMediaType.APPLICATION_XML_UTF8)
    public StreamingOutput xquery(@PathParam("did") String did, @DefaultValue("") @QueryParam("query") String query, @DefaultValue("") @QueryParam("sort") String sort, @DefaultValue("0") @QueryParam("skip") String skip, @DefaultValue("10") @QueryParam("offset") String offset,
                                  @QueryParam("indent") final boolean indent, @QueryParam("debug") boolean debug, @Context HttpRequest request) throws IOException, ParseException {

        MultivaluedMap<String, String> map = request.getUri().getQueryParameters();
        final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                OutputStreamWriter writer = new OutputStreamWriter(output);
                OutputHandler ohandler = OutputHandler.createXml(writer, indent);
                ohandler.out(articles, new JsonObject(), new JsonObject());
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
        final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                OutputStreamWriter writer = new OutputStreamWriter(output);
                OutputHandler ohandler = OutputHandler.createCsv(writer);
                ohandler.out(articles, new JsonObject(), new JsonObject());
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
            final XIterable<ArticleX> articles = findArticle(did, query, sort, skip, offset, request, map).find();

            StringWriter writer = new StringWriter();
            String resourceName = "/domain/" + did + "/article" + ".template";
            qengine.merge(resourceName, MapUtil.<String, Object>chainMap().put("articles", articles).put("params", map).toMap(), writer);

            return writer.toString();
        } catch (org.apache.velocity.exception.ParseErrorException tex) {
            tex.printStackTrace();
            return tex.getMessage();
        }
    }


    @GET
    @Path("/{did}/list")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject listArticle(@PathParam("did") final String did, @QueryParam("query") final String query, @DefaultValue("101") @QueryParam("offset") final int offset) throws IOException {
        XIterable<ArticleX> articles = dsub.findDomain(did).datas().articles().where(query).offset(offset).find();
        JsonObject result = JsonObject.create();
        JsonArray jarray = new JsonArray();
        result.put("result", jarray);

        for (ArticleX article : articles) {
            JsonArray rowArray = new JsonArray().add(new JsonPrimitive(article.artId())).add(new JsonPrimitive(article.catId())).add(new JsonPrimitive(article.asString(Def.Article.Subject)))
                    .add(new JsonPrimitive(article.asString(Def.Article.ModDay)));
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


    @GET
    @Path("/{did}/thumbimg/{catid}/{artid}.stream")
    public UncertainOutput viewThumbImage() {
        return new UncertainOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
            }

            @Override
            public MediaType getMediaType() {
                return ExtMediaType.IMAGE_PNG_TYPE;
            }
        };
    }

    @GET
    @Path("/{did}/afield/{catid}/{artid}/{aid}.stream")
    public UncertainOutput viewAfieldResource() {
        return new UncertainOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
            }

            @Override
            public MediaType getMediaType() {
                return ExtMediaType.IMAGE_PNG_TYPE;
            }
        };
    }

    @GET
    @Path("/{did}/content/{catid}/{artid}/{resourceid}.stream")
    public UncertainOutput viewContentImage() {
        return new UncertainOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
            }

            @Override
            public MediaType getMediaType() {
                return ExtMediaType.IMAGE_PNG_TYPE;
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
