package net.ion.external.domain;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import net.ion.external.ics.bean.AfieldValueX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.BeanX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestAfieldValue extends TestBaseDomain{
	
	public void testAfield() throws Exception {
		ArticleX a1206381 = domain.article("dynamic", 1206381) ;
		
		AfieldValueX afield = a1206381.asAfield("lyn_string");
		afield.debugPrint();
		
		assertEquals("String", afield.typeCd()) ;
	}
	
	public void testFileAfeld() throws Exception {
		ArticleX article = domain.article("dynamic", 1206380) ;
		InputStream input = article.asAfield("lyn_image").dataStream() ;
		
		assertEquals(true, input != BeanX.BLANKSTREAM);
		
		article.asAfield("booltest").asBoolean() ;
		IOUtil.close(input);
	}
	
	public void testAtArticle() throws Exception {
		ArticleX article = domain.article("dynamic", 1206380) ;
		assertEquals(true, article.asBoolean("booltest")) ;
		
		assertEquals("/2014/06/13/IMG_1210_1.JPG", article.asString("lyn_image")) ;
	}
	
	public void testAsObject() throws Exception {
		ArticleX article = domain.article("dynamic", 1206380) ;
		
		assertEquals(Boolean.TRUE, article.asAfield("booltest").asObject()) ;
		assertEquals(0L, article.asAfield("num_lyn").asObject()) ;
		assertEquals("test1234", article.asAfield("lyn_string").asObject()) ;
		
		// 
		ArticleX find =  article.asAfield("booltest").article() ;
		assertEquals(article, find);
		assertEquals(1206380, find.artId());
		assertEquals("dynamic", find.catId());
	}
	
	
	public void testAsObject2() throws Exception {
		domain.addSiteCategory("sssc223", false) ;
		
		ArticleX article = domain.article("sssc223", 1206952) ; //sssc223 1206952
		
		assertEquals("", article.asAfield("ssc_set").asString()) ;
		assertEquals("13000", article.asAfield("ssc_2").asObject());
		assertEquals("13000", article.asAfield("ssc_set").asMap().get("ssc_2")) ;
		
		
		
		Writer sw = new StringWriter() ;
		OutputHandler ohandler = OutputHandler.createJson(sw);
		article.out(ohandler) ;
		
		Debug.line(sw);
	}
	
	
	
	

}
