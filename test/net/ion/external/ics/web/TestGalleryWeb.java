package net.ion.external.ics.web;

import org.jboss.resteasy.util.HttpHeaderNames;

import net.ion.cms.env.ICSCraken;
import net.ion.framework.util.Debug;
import net.ion.nradon.stub.StubHttpResponse;
import net.ion.radon.client.StubServer;
import junit.framework.TestCase;

public class TestGalleryWeb extends TestCase {

	public void testSearch() throws Exception {
		StubServer ss = StubServer.create(GalleryWeb.class) ;
		ss.treeContext().putAttribute("craken", ICSCraken.create()) ;
		StubHttpResponse response = ss.request("/gallery/search/sm?limit=10").get();
		
		Debug.line(response.charset(), response.header(HttpHeaderNames.CONTENT_TYPE));
		
		Debug.line(response.contentsString());
	}
	
}
