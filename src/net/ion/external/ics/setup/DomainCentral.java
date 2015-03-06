package net.ion.external.ics.setup;

import java.io.IOException;

import net.ion.craken.Craken;
import net.ion.external.domain.DomainSub;
import net.ion.framework.db.DBController;
import net.ion.framework.db.IDBController;
import net.ion.framework.db.manager.OracleDBManager;

public class DomainCentral {

	private IDBController idc ;
	private Craken craken ;
	public DomainCentral(OracleDBManager dbm, Craken craken) {
		this.idc = new DBController(dbm) ;
		this.craken = craken ;
	}

	
	public static DomainCentral createMaster(OracleDBManager dbm, Craken craken) {
		return new DomainCentral(dbm, craken);
	}

	
	public static DomainSub createSub(Craken craken) throws IOException {
		return new DomainSub(craken);
	}


}
