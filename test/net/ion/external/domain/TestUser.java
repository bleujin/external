package net.ion.external.domain;

import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.UserX;

public class TestUser extends TestBaseDomain{

	public void testReset() throws Exception {
		domain.resetUser() ;
		
		UserX user = domain.datas().findUser("ksh") ;
		assertEquals("ksh", user.asString("password"));

		
		ArticleX a1206381 = domain.datas().article("dynamic", 1206381) ;
		assertEquals("yucea", a1206381.regUser().userId()) ;
	}
}
