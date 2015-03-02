package net.ion.external.ics.bean;

import net.ion.external.ics.bean.AfieldX;

public class TestAfieldX extends TestPOJOBase{

	
	public void testFindAfield() throws Exception {
		AfieldX afield = rc.findAfield("year") ;
		
		assertEquals("year", afield.afieldId());
		assertEquals("String", afield.typeCd());
		assertEquals("Year", afield.asString("afieldnm"));
	}
	
	
}
