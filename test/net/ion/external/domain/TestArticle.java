package net.ion.external.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestArticle extends TestBaseDomain {


	public void testArticles() throws Exception {
		ArticleChildrenX articles = domain.datas().articles() ;
	
		assertEquals(11, articles.find().count());	
	}
	
	
	public void testFindArticle() throws Exception {
		ArticleX a1206381 = domain.datas().article("dynamic", 1206381) ;
		assertEquals(true, a1206381.exists());
		
		assertEquals(1206381, a1206381.artId());
		
		a1206381.debugPrint();
	}
	
	
	public void testThumb() throws Exception {
		ArticleX a1206381 = domain.datas().article("dynamic", 1206381) ;
		InputStream inputStream = a1206381.thumbnailStream() ;
		
		assertEquals(true, inputStream != ArticleX.BLANKSTREAM);
		IOUtil.close(inputStream);
	}
	

	public void testArticleImage() throws Exception {
		ArticleX a1207152 = domain.datas().article("dynamic", 1207152) ;
		assertEquals(true, a1207152.thumbnailStream() == ArticleX.BLANKSTREAM);
		
		InputStream input = a1207152.contentStream("/2014/12/12/ibr_test/IMG_1695.JPG") ;
		assertEquals(true, input != ArticleX.BLANKSTREAM);
		IOUtil.close(input);
	}
	
	
	public void testOutput() throws Exception {
		Writer sw = new StringWriter();
		OutputHandler ohandler = OutputHandler.createJson(sw, true);
		domain.datas().article("dynamic", 1207152).afields(true).out(ohandler) ; 
		Debug.line(sw);
	}

    public void testArticleContent() throws IOException {
        // given
        ArticleX article = domain.datas().article("dynamic", 1207152);;

        // when
        StringWriter sw = new StringWriter();
        OutputHandler ohandler = OutputHandler.createJson(sw, true);

        article.out(ohandler) ;

        // then
        Debug.line(sw.toString());
    }
	
}
