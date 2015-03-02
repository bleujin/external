package net.ion.external.ics.bean;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;

public class AfieldX extends BeanX{

	public AfieldX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}

	public static AfieldX create(ICSCraken rc, ReadNode node) {
		return new AfieldX(rc, node);
	}

	public String afieldId() {
		return asString(Def.Afield.AfieldId);
	}
	
	public String typeCd() {
		return asString(Def.Afield.TypeCd);
	}	

}
