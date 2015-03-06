package net.ion.external.domain;

import net.ion.external.ics.bean.TemplateChildrenX;
import net.ion.external.ics.bean.TemplateX;

public class TestTemplate extends TestBaseDomain{

	
	public void testListTemplate() throws Exception {
		TemplateChildrenX templates = domain.templates() ;
		TemplateX template = templates.listTemplateBySeq(0) ;
		
		assertEquals("index.html", template.asString("filename"));
		assertEquals("dynamic", template.catId()) ;
		
		assertEquals(true, template.isList()) ;
	}
	
	
	
	
}
