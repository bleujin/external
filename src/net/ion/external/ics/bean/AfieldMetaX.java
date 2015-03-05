package net.ion.external.ics.bean;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;

public class AfieldMetaX extends BeanX{

	public AfieldMetaX(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public static AfieldMetaX create(Domain domain, ReadNode node) {
		return new AfieldMetaX(domain, node);
	}

	public String afieldId() {
		return asString(Def.Afield.AfieldId);
	}
	
	public String typeCd() {
		return asString(Def.Afield.TypeCd);
	}	

}
