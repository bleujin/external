package net.ion.external.ics.bean;

import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.XIterable;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;


public class TestArticleX extends TestPOJOBase{

	public void testFindArticleX() throws Exception {
		ArticleX article = rc.findSiteCategory("cxm").article(7756) ;
		
		assertEquals("cxm", article.catId());
		assertEquals(7756, article.artId());
		
		assertEquals("bleujin", article.asString("userid"));
		assertEquals(20, article.asInt("age"));
		
	}
	
	
	

	public void testFindCategory() throws Exception {
		ArticleX article = findArticle("cxm", 7756) ;

		SiteCategoryX category = article.category();
		
		assertEquals("cxm", category.catId());
		assertEquals("/dev/cxm", category.pathById());
		assertEquals("/develop team/cxm team", category.pathByName());
	}

	
	public void testRelated() throws Exception {
		ArticleX article = findArticle("cxm", 7756) ;
		XIterable<ArticleX> rels = article.relateds() ;
		
		rels.debugPrint();
		
		assertEquals(2, rels.count());
	}
	
	
	public void testXIterable() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		XIterable<ArticleX> articles = category.articles().find() ;
		
		
		articles.debugPrint();
	}

	
	public void testStream() throws Exception {
		ArticleX article = findArticle("cxm", 7756) ;
		
		Debug.line(IOUtil.toStringWithClose(article.asStream("data"))) ;
	}
	
}
