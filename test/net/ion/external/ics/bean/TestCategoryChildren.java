package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.SiteCategoryX;

public class TestCategoryChildren extends TestPOJOBase {

	public void testChildren() throws Exception {
		CategoryChildrenX<SiteCategoryX> children = rc.findSiteCategory("dept").children();

		assertEquals(2, children.find().count());
	}
	
	public void testIncludeLeaf() throws Exception {
		CategoryChildrenX<SiteCategoryX> children = rc.findSiteCategory("dept").children(true);
		
		children.find().debugPrint();
	}
	
	public void testFrom() throws Exception {
		SiteCategoryX dept = rc.findSiteCategory("dept") ;
		assertEquals(true, dept == dept.children().from());
	}
	

}
