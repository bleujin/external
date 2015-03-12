package net.ion.external.ics.web.domain;

import java.io.StringWriter;
import java.net.URLEncoder;

import net.ion.external.domain.TestBaseDomain;
import net.ion.external.ics.QueryTemplateEngine;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.XIterable;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
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

	public void testFirst() throws Exception {
		XIterable<GalleryX> gallerys = domain.datas().gallerys().where("catid=aaaa").sort("").skip(0).offset(10).find() ;
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
	
	public void testList() throws Exception {
		
	}
	
	
	
	
}
