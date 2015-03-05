package net.ion.external.domain;

import java.io.InputStream;

import net.ion.external.ics.bean.AfieldValueX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.BeanX;
import net.ion.framework.util.IOUtil;

public class TestAfield extends TestBaseDomain{
	
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

}
