package net.ion.external.domain;

import java.io.File;

import junit.framework.TestCase;
import net.ion.craken.Craken;
import net.ion.framework.db.manager.OracleDBManager;

public class TestBaseDomain extends TestCase{

	private OracleDBManager dbm;
	private DomainMaster dmaster;
	
	protected Craken icraken ;
	protected DomainSub dsub;
	protected Domain domain;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
		
		this.icraken = Craken.test() ;
		this.dmaster = DomainMaster.create(dbm, icraken)
					.artImageRoot(new File("./resource/uploadfiles/artimage"))
					.galleryRoot(new File("./resource/uploadfiles/gallery"))
					.afieldFileRoot(new File("./resource/uploadfiles/afieldfile"));
		
		this.dsub = DomainSub.create(icraken) ;
		dsub.createDomain("zdm") ;
		
		assertEquals(true, dsub.existDomain("zdm")) ;
		this.domain = dsub.findDomain("zdm")
					.addSiteCategory("dynamic", false);
	}
	
	@Override
	protected void tearDown() throws Exception {
		icraken.stop();
		dbm.destroyPool(null);
		super.tearDown();
	}
	
}
