package net.ion.external.domain;

import java.io.StringWriter;
import java.io.Writer;

import net.ion.external.ics.bean.AfieldMetaX;
import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.XIterable;
import net.ion.framework.util.Debug;

public class TestSiteCategory extends TestBaseDomain{
	
	public void testFindCategory() throws Exception {
		CategoryChildrenX<SiteCategoryX> categorys = domain.scategorys() ;
		
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
	
	public void testAfields() throws Exception {
		domain.addSiteCategory("dynamic_lyn", true) ;
		
		SiteCategoryX scategory = domain.scategory("dynamic_lyn");
		XIterable<AfieldMetaX> afields = scategory.afieldMetas();
		AfieldMetaX afield = afields.findByKey("num_lyn") ;
		assertEquals("Number", afield.typeCd());
		
		assertEquals(2, scategory.article(1207020).afields().count()) ;
		assertEquals(3, scategory.article(1207020).afields(true).count()) ;
		
		assertEquals("Date", scategory.article(1207020).afields(true).findByKey("date_lyn").typeCd()) ;
		
	}
	
	
	
	

}
