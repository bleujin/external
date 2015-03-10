package net.ion.external.domain;

import net.ion.external.ics.bean.AfieldMetaX;
import net.ion.external.ics.bean.XIterable;

public class TestAfieldMeta extends TestBaseDomain{

	
	public void testFindMetaInfo() throws Exception {
		AfieldMetaX ameta = domain.datas().afieldMeta("ssc_set") ;

		assertEquals("Set", ameta.typeCd());
		XIterable<AfieldMetaX> schildren = ameta.children() ;
		
		assertEquals(20, schildren.count());
	}
	
	
}
