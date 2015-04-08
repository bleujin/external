package net.ion.external.ics.web.domain;

import java.io.StringWriter;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.util.HttpHeaderNames;

import net.ion.external.domain.TestBaseDomain;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.XIterable;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.nradon.stub.StubHttpResponse;
import net.ion.radon.client.StubServer;

public class TestGalleryWeb extends TestBaseDomain {
	
	private StubServer ss;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.ss = StubServer.create(GalleryWeb.class) ;
		ss.treeContext().putAttribute(DomainEntry.EntryName, DomainEntry.test(dsub)) ;
		ss.treeContext().putAttribute(QueryTemplateEngine.EntryName, QueryTemplateEngine.create("my.craken", icraken.login())) ;
		domain.addGalleryCategory("aaaa", true) ;
	}

	public void testFirstWhere() throws Exception {
		XIterable<GalleryX> gallerys = domain.datas().gallerys().where("catid=aaaa").sort("").skip(0).offset(10).find() ;
		StringWriter sw = new StringWriter();
		OutputHandler output = OutputHandler.createJson(sw, true) ;
		output.out(gallerys, new JsonObject(), new JsonObject()) ;
		Debug.line(sw);
	}
	
	public void testQuery() throws Exception {
		XIterable<GalleryX> gallerys = domain.datas().gallerys().query("530").sort("").skip(0).offset(10).find() ;
		StringWriter sw = new StringWriter();
		OutputHandler output = OutputHandler.createJson(sw, true) ;
		output.out(gallerys, new JsonObject(), new JsonObject()) ;
		Debug.line(sw);
	}
	
	public void testJsonQuery() throws Exception {
		String encoded = URLEncoder.encode("catid=aaaa") ;
		String json = ss.request("/gallery/zdm/query.json?query=" + encoded).get().contentsString() ;
		String pjson = new GsonBuilder().setPrettyPrinting().create().toJson(JsonObject.fromString(json)) ;
		Debug.line(pjson);
	}
	
	
	public void testViewImage() throws Exception {
		 StubHttpResponse response = ss.request("/gallery/zdm/view/11000").get() ;
		 assertEquals(200, response.status());
		 assertEquals(ExtMediaType.IMAGE_JPEG.toString(), response.header(HttpHeaderNames.CONTENT_TYPE));

		 
		 // when not found
		 response = ss.request("/gallery/zdm/view/11002").get() ;
		 assertEquals(404, response.status());
		 
		 Debug.line(response.status(), response.header(HttpHeaderNames.CONTENT_TYPE));
	}
	
	
	public void testList() throws Exception {
		MediaType mtype = MediaType.valueOf("text/plain; charset=utf-8") ;
		Debug.line(mtype);
	}
	
	
	
	
}
