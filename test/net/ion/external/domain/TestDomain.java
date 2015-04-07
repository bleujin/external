package net.ion.external.domain;

import java.util.Map;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class TestDomain extends TestBaseDomain{

	private ReadSession session;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.session = icraken.login() ;
	}
	
	public void testWhenAddCategory() throws Exception {
		assertEquals(true, session.exists("/domain/zdm")) ;
		
		assertEquals(false, session.pathBy("/domain/zdm/scat/dynamic").isGhost()) ;
		assertEquals(Boolean.FALSE, session.pathBy("/domain/zdm/scat/dynamic").property("includesub").asBoolean()) ;
		
		session.pathBy("/datas/log").walkChildren().debugPrint();
		
		Map<String, Boolean> resultMap = domain.info().siteCategory(new DomainNodeInfoHandler<Map<String, Boolean>>(){
			@Override
			public Map<String, Boolean> handle(ReadChildren scats) {
				Map<String, Boolean> result = MapUtil.newMap() ;
				for(ReadNode i : scats.iterator()){
					result.put(i.fqn().name(), i.property("includesub").asBoolean()) ;
				}
				return result;
			}
		}) ;
		

		Debug.line(resultMap);
		assertEquals(Boolean.FALSE, resultMap.get("dynamic"));
		assertEquals(1, resultMap.size());
		
		
	}

	
	public void testWhenRemoveCategory() throws Exception {
		domain.addSiteCategory("dynamic", true) ;
		
		
		ArticleChildrenX articles = domain.datas().articles() ;
		assertEquals(11, articles.find().count());	

		assertEquals(1, domain.datas().scategorys().find().count()) ;
		
		domain.removeSiteCategory("dynamic") ;
		assertEquals(0, domain.datas().scategorys().find().count()) ;
		assertEquals(0, domain.datas().articles().find().count()) ;
		assertEquals(0, domain.datas().templates().find().count()) ;
	}
	
	
	public void testWhenRemoveDomain() throws Exception {
		domain.addSiteCategory("dynamic", true) ;
		ArticleChildrenX articles = domain.datas().articles() ;
		assertEquals(11, articles.find().count());	

		dsub.removeDomain("zdm");
		assertEquals(false, dsub.existDomain("zdm")) ;
		
		dsub.createDomain("zdm");
		domain = dsub.findDomain("zdm") ;
		assertEquals(0, domain.datas().scategorys().find().count()) ;
		
	}
	

}
