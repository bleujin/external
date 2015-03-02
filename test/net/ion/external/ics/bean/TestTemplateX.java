package net.ion.external.ics.bean;

import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.TemplateChildrenX;
import net.ion.external.ics.bean.TemplateX;
import net.ion.external.ics.bean.XIterable;

public class TestTemplateX extends TestPOJOBase{

	public void testFindTemplate() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		TemplateChildrenX tpls = category.templates() ;
		
		tpls.find().ascending("tplid").reverse() ;
		tpls.find().debugPrint(); 
	}
	
	
	public void testByKey() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		
		TemplateX template = category.template(22402) ;
		assertEquals(true, template.exists());
		
	}
	
	public void testFindByKey() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		TemplateChildrenX tpls = category.templates() ;

		TemplateX tpl = tpls.findById(22402) ;
		tpl.debugPrint(); 
		
		TemplateX notfound = tpls.findById(0) ;
		assertEquals(false, notfound.exists());
	}
	
	public void testListTemplates() throws Exception {
		SiteCategoryX cxm = rc.findSiteCategory("cxm") ;
		XIterable<TemplateX> tpls = cxm.templates().selectList().find() ;
		
		tpls.debugPrint();
	}
	
	public void testListFileName() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		TemplateChildrenX tpls = category.templates() ;
		
		assertEquals("test.html", tpls.listTemplate(22402).fileName()) ;
		assertEquals("test2.html", tpls.listTemplate(0).fileName()) ;
		
		assertEquals("test2.html", tpls.listTemplate(12345).fileName()) ;
	}
	
	
	public void testStoryFileName() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		TemplateChildrenX tpls = category.templates() ;
		
		assertEquals("my7756.html", tpls.storyTemplate(20245).fileName(7756)) ;
		assertEquals("20245_0.html", tpls.storyTemplate(0).fileName(7780)) ; //  not exist article
		assertEquals("20245_0.html", tpls.storyTemplate(0).fileName(1234)) ;

	}

	
	
}
