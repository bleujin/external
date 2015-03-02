package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.GalleryCategoryX;

public class TestGalleryCategoryX extends TestPOJOBase {

	public void testGalleryCategory() throws IOException {
		CategoryChildrenX<GalleryCategoryX> children = rc.findGalleryCategory("idol").children(true);
		
		assertEquals(2, children.find().count()) ;
		children.find().debugPrint();
	}

}
