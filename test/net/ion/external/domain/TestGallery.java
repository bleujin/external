package net.ion.external.domain;

import java.io.InputStream;

import net.ion.external.ics.bean.BeanX;
import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.GalleryCategoryX;
import net.ion.external.ics.bean.GalleryChildrenX;
import net.ion.external.ics.bean.GalleryX;
import net.ion.framework.util.IOUtil;

public class TestGallery extends TestBaseDomain{

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		domain.addGalleryCategory("aaaa", true);
	}
	
	public void testCategory() throws Exception {
		CategoryChildrenX<GalleryCategoryX> children = domain.datas().gcategorys() ;
		
		assertEquals(2, children.find().count());
		
		GalleryCategoryX gcat = domain.datas().gcategory("bbbb");
		assertEquals("bbbb", gcat.catId());
	}
	
	public void testGallery() throws Exception {
		GalleryCategoryX gcat = domain.datas().gcategory("bbbb");
		GalleryChildrenX children = gcat.galleries() ;
		
		assertEquals(1, children.find().count()) ;
		
		GalleryX gallery = gcat.gallery(11000) ;
		
		gallery.debugPrint();
		assertEquals(11000, gallery.galId()) ;
		
		InputStream input = gallery.dataStream() ;
		assertEquals(true, input != BeanX.BLANKSTREAM) ;
		IOUtil.close(input);
	}
	
	public void testResize() throws Exception {
		GalleryCategoryX gcat = domain.datas().gcategory("bbbb");
		GalleryX gallery = gcat.gallery(11000) ;
		
		InputStream input = gallery.dataStreamWithSize(100, 100);
		
		assertEquals(true, input != BeanX.BLANKSTREAM) ;
		IOUtil.close(input);
	}
	
	public void testCrop() throws Exception {
		GalleryCategoryX gcat = domain.datas().gcategory("bbbb");
		GalleryX gallery = gcat.gallery(11000) ;
		
		gallery.dataStreamWithSize(100, 100);
	}
	
	
	
	
}