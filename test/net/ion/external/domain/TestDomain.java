package net.ion.external.domain;

import net.ion.external.ics.bean.ArticleChildrenX;

public class TestDomain extends TestBaseDomain{

	public void testWhenRemoveCategory() throws Exception {
		domain.addSiteCategory("dynamic", true) ;
		
		ArticleChildrenX articles = domain.articles() ;
		assertEquals(11, articles.find().count());	

		domain.removeSiteCategory("dynamic", false) ;
	}
}
