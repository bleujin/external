package net.ion.external.ics.setup;

import java.io.IOException;

import net.ion.external.ICSSubCraken;
import net.ion.external.domain.DomainSub;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.manager.OracleDBManager;

public class DomainCentral {

	private IDBController idc ;
	private ICSSubCraken craken ;
	public DomainCentral(OracleDBManager dbm, ICSSubCraken craken) {
		this.idc = new DBController(dbm) ;
		this.craken = craken ;
	}

	
	public static DomainCentral createMaster(OracleDBManager dbm, ICSSubCraken craken) {
		return new DomainCentral(dbm, craken);
	}

	
	public static DomainSub createSub(ICSSubCraken craken) throws IOException {
		return new DomainSub(craken);
	}


}
