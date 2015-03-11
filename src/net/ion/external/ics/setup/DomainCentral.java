package net.ion.external.ics.setup;

import java.io.IOException;

import net.ion.craken.ICSCraken;
import net.ion.external.domain.DomainSub;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.manager.OracleDBManager;

public class DomainCentral {

	private IDBController idc ;
	private ICSCraken craken ;
	public DomainCentral(OracleDBManager dbm, ICSCraken craken) {
		this.idc = new DBController(dbm) ;
		this.craken = craken ;
	}

	
	public static DomainCentral createMaster(OracleDBManager dbm, ICSCraken craken) {
		return new DomainCentral(dbm, craken);
	}

	
	public static DomainSub createSub(ICSCraken craken) throws IOException {
		return new DomainSub(craken);
	}


}
