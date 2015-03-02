package net.ion.external.ics.bean;

import net.ion.external.ics.bean.UserX;

public class TestUserX extends TestPOJOBase {

	
	public void testFindUser() throws Exception {
		UserX user = rc.findUser("bleujin") ;
		assertEquals(true, user.exists());
	}
	
	public void testIsVerifier() throws Exception {
		
		UserX user = rc.findUser("bleujin") ;
		
		assertEquals(true, user.isVerify("redf")) ;
		assertEquals(false, user.isVerify("bleujin")) ;
	}
	
	
}
