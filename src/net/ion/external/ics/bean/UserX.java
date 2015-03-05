package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;

public class UserX extends BeanX{

	public UserX(Domain domain, ReadNode node) {
		super(domain, node);
	}
	
	public final static UserX create(Domain domain, ReadNode node){
		return new UserX(domain, node) ;
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
