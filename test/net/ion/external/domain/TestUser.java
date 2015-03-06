package net.ion.external.domain;

import oracle.net.aso.a;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.UserX;

public class TestUser extends TestBaseDomain{

	public void testReset() throws Exception {
		domain.resetUser() ;
		
		UserX user = domain.findUser("ksh") ;
		assertEquals("ksh", user.asString("password"));

		
		ArticleX a1206381 = domain.article("dynamic", 1206381) ;
		assertEquals("yucea", a1206381.regUser().userId()) ;
	}
}
