package net.ion.external.ics.setup;

import java.io.IOException;

import net.ion.external.ICSSampleCraken;
import net.ion.external.domain.DomainSub;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.manager.OracleDBManager;

public class DomainCentral {

	private IDBController idc ;
	private ICSSampleCraken craken ;
	public DomainCentral(OracleDBManager dbm, ICSSampleCraken craken) {
		this.idc = new DBController(dbm) ;
		this.craken = craken ;
	}

	
	public static DomainCentral createMaster(OracleDBManager dbm, ICSSampleCraken craken) {
		return new DomainCentral(dbm, craken);
	}

	
	public static DomainSub createSub(ICSSampleCraken craken) throws IOException {
		return new DomainSub(craken);
	}


}
