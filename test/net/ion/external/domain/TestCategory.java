package net.ion.external.domain;

import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.SiteCategoryX;

public class TestCategory extends TestBaseDomain{
	
	public void testFindCategory() throws Exception {
		CategoryChildrenX<SiteCategoryX> categorys = domain.categorys() ;
		
		assertEquals(true, categorys != null);

		assertEquals(1, categorys.find().count()) ;
		
		SiteCategoryX category = domain.scategory("dynamic") ;
		assertEquals("dynamic", category.catId()) ;
	}
	
	public void testIncludeSub() throws Exception {
		domain.addSiteCategory("r_sc_usion", true) ;
		
		SiteCategoryX category = domain.scategory("r_us_pro") ;
		assertEquals("r_us_pro", category.catId());
		
		CategoryChildrenX<SiteCategoryX> children = domain.scategory("r_sc_usion").chidren() ;
		assertEquals(8, children.find().count());
	}
	
	public void testProperty() throws Exception {
		SiteCategoryX category = domain.scategory("dynamic") ;
		assertEquals("dynamic", category.asString("phydirnm")) ;
	}
	
	
	public void testTemplates() throws Exception {
		SiteCategoryX category = domain.scategory("dynamic") ;
		
		assertEquals(true, category.templates().find().count() > 0) ;; 
	}
	
	public void testArticles() throws Exception {
		SiteCategoryX category = domain.scategory("dynamic") ;

		assertEquals(true, category.articles().find().count() > 0) ;
	}
	
	
	
	

}
