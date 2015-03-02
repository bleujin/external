package net.ion.external.ics.bean;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import net.ion.external.ics.bean.GalleryX;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;


public class TestGalleryX extends TestPOJOBase{

	
	public void testFindGallery() throws Exception {
		GalleryX gallery = findGallery("girlsday", 10521) ;
		assertEquals("girlsday", gallery.catId());
		
		assertEquals("girlsday", gallery.category().catId()) ;
	}
	
	public void testMinA() throws Exception {
		GalleryX minA = findGallery("girlsday", 10521) ;
		
		InputStream input = minA.dataStream() ;
		assertEquals(true, input.read() > 0);
		IOUtil.close(input);
		
		
		assertEquals("jpg", minA.typeCd()) ;
	}
	
	
	
	
}
