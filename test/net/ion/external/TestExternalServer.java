package net.ion.external;

import java.io.File;

import junit.framework.TestCase;
import net.ion.external.ExternalServer;
import net.ion.external.config.builder.ConfigBuilder;
import net.ion.external.domain.DomainSampleMaster;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.InfinityThread;

public class TestExternalServer extends TestCase {


    public void testRun() throws Exception {
		final ExternalServer server = ExternalServer.create(ConfigBuilder.createDefault(9001).build()) ;

		// simmul dmaster
		DomainSampleMaster dmaster = DomainSampleMaster.create(new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6"), server.craken())
				.artImageRoot(new File("./resource/uploadfiles/artimage"))
				.galleryRoot(new File("./resource/uploadfiles/gallery"))
				.afieldFileRoot(new File("./resource/uploadfiles/afieldfile")) ;
		
		
		server.start() ;
        new InfinityThread().startNJoin();
    }
    
    
    public void testDistRun() throws Exception {
		final ExternalServer server = ExternalServer.create(ConfigBuilder.create("./resource/config/es-config.xml").build()) ;

		server.start() ;
		
		
		
        new InfinityThread().startNJoin();
	}
}
