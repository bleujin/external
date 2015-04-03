package net.ion.external.ics.web.domain;

import java.io.StringWriter;
import java.net.URLEncoder;

import org.apache.http.client.utils.URLEncodedUtils;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.domain.TestBaseDomain;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.framework.parse.gson.Gson;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.radon.client.StubServer;
import junit.framework.TestCase;

public class TestArticleWeb extends TestBaseDomain {
	
	private StubServer ss;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.ss = StubServer.create(ArticleWeb.class) ;
		ss.treeContext().putAttribute(DomainEntry.EntryName, DomainEntry.test(dsub)) ;
		ss.treeContext().putAttribute(QueryTemplateEngine.EntryName, QueryTemplateEngine.create("my.craken", icraken.login())) ;
	}
	
	public void testJsonQuery() throws Exception {
		String encoded = URLEncoder.encode("artid=1207152 && catid=dynamic") ;
		String json = ss.request("/article/zdm/query.json?query=" + encoded).get().contentsString() ;
		String pjson = new GsonBuilder().setPrettyPrinting().create().toJson(JsonObject.fromString(json)) ;
		Debug.line(pjson);
	}

	public void testXMLQuery() throws Exception {
		String encoded = URLEncoder.encode("artid=1207152 && catid=dynamic") ;
		String json = ss.request("/article/zdm/query.xml?query=" + encoded + "&indent=true").get().contentsString() ;
		Debug.line(json);
	}

	
	public void testTemplateQuery() throws Exception {
		ReadSession session = super.icraken.login() ;
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/zdm/article").property("template", IOUtil.toStringWithClose(getClass().getResourceAsStream("default.template"))) ;
				return null;
			}
		}) ;

		String encoded = URLEncoder.encode("artid=1207152 && catid=dynamic") ;
		String content = ss.request("/article/zdm/query.template?query=" + encoded).get().contentsString() ;

		Debug.line(content);
	}
	
	
	public void testList() throws Exception {
		String json = ss.request("/article/zdm/list").get().contentsString() ;
		String pjson = new GsonBuilder().setPrettyPrinting().create().toJson(JsonObject.fromString(json)) ;
		Debug.line(pjson);
	}
	
	
	
	public void testView() throws Exception {
		ArticleX article = dsub.findDomain("zdm").datas().article("dynamic", 1207152); // 1206381, 1207152
		
		StringWriter writer = new StringWriter() ;
		OutputHandler output = OutputHandler.createJson(writer, true) ;
		article.out(output) ;
		Debug.line(writer);

		
		StringWriter swriter = new StringWriter() ;
		article.jsonWrite(swriter) ;

		Debug.line(swriter);
	}

	
}
