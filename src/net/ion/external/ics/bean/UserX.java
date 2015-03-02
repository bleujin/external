package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;

public class UserX extends BeanX{

	public UserX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}
	
	public final static UserX create(ICSCraken rc, ReadNode node){
		return new UserX(rc, node) ;
	}

	public String userId(){
		return asString(Def.User.UserId) ;
	}

	public boolean isVerify(String pwd) throws IOException {
		return exists() && node().isMatch(Def.User.VerifyKey, pwd);
	}

	public boolean isVerify(char[] secret) throws IOException {
		return isVerify(new String(secret)) ;
	}
	
	
}
