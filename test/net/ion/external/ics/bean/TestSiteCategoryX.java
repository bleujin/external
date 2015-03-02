package net.ion.external.ics.bean;

import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.framework.util.Debug;

public class TestSiteCategoryX extends TestPOJOBase {
	
//		Map params = MapUtil.newMap() ;
//		params.put("aradon.result.format", "json") ;
//		params.put("aradon.page.listNum", "10") ;
//		params.put("aradon.page.pageNo", "1") ;
//		params.put("catid", "bleujin") ;

	public void testFind() throws Exception {
		SiteCategoryX cxm = rc.findSiteCategory("cxm") ;

		assertEquals(true, cxm.exists());
		assertEquals(false, rc.findSiteCategory("notfound").exists());
	}
	
	public void testParent() throws Exception {
		SiteCategoryX cxm = rc.findSiteCategory("cxm") ;
		SiteCategoryX dev = cxm.parent() ;
		
		assertEquals("cxm", cxm.catId());
		assertEquals("dev", dev.catId());
	}
	
	public void testPathById() throws Exception {
		SiteCategoryX cxm = rc.findSiteCategory("cxm") ;
		Debug.line(cxm.pathById(), cxm.pathByName()) ;
	}
	
	
	public void testArticles() throws Exception {
		rc.findSiteCategory("dev").articles().debugPrint(); 
	}
	
	

	
	
	
}
