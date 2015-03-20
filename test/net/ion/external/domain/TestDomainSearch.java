package net.ion.external.domain;

import com.sun.xml.internal.ws.api.WSService;

import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.ICSSampleCraken;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;

public class TestDomainSearch extends TestCase {

	
	private ReadSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ICSSampleCraken ic = ICSSampleCraken.test() ;
		this.session = ic.login() ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		session.workspace().repository().shutdown() ;
		super.tearDown();
	}
	
	
	public void testRefSearch() throws Exception {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/dept/dev").property("name", "dev") ;
				wsession.pathBy("/dept/cmx").property("name", "cmx") ;
				
				
				wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTos("dept", "/dept/dev", "/dept/cmx") ;
				wsession.pathBy("/emp/air").property("name", "air").refTos("dept", "/dept/cmx") ;
				return null;
			}
		}) ;
		assertEquals(2, session.pathBy("/dept/cmx").refsToMe("dept").find().size()); 
		
		final IteratorList<ReadNode> iter = session.pathBy("/dept/cmx").refsToMe("dept").find().iterator() ;
		
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				while(iter.hasNext()){
					ReadNode node = iter.next() ;
//					if (wsession.pathBy(node.fqn()).refs("dept").count() == 1){
//						wsession.pathBy(node.fqn()).removeSelf() ;
//					} else {
//						wsession.pathBy(node.fqn()).unRefTos("dept", "/dept/cmx") ;
//					}
					wsession.pathBy(node.fqn()).unRefTos("dept", "/dept/cmx") ;

				}
				return null;
			}
		}) ;
		
	 	assertEquals(0, session.pathBy("/dept/cmx").refsToMe("dept").find().size()) ;
	 	assertEquals(true, session.exists("/emp/bleujin")) ;
//	 	assertEquals(true, session.exists("/emp/air")) ;
	}

	
	
}
