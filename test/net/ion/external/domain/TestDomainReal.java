package net.ion.external.domain;

import java.util.Map;

import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.framework.util.ObjectId;
import junit.framework.TestCase;

public class TestDomainReal extends TestCase {

	
	private RepositoryImpl r;
	private ReadSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.r = RepositoryImpl.inmemoryCreateWithTest() ;
		this.session = r.login("test") ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		r.shutdown() ;
		super.tearDown();
	}
	
	public void testCddChain() throws Exception {
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/emps/{userid}";
			}
			
			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String userId = resolveMap.get("userid") ;
				return new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						wsession.pathBy("/logs", new ObjectId()).property("event", "fired").property("userid", userId) ;
						return null;
					}
				};
			}
			
			@Override
			public TransactionJob<Void> deleted(Map<String, String> arg0, CDDRemovedEvent arg1) {
				// TODO Auto-generated method stub
				return null;
			}
		}) ;

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/emps/bleujin").property("userid", "bleujin") ;
				return null;
			}
		}) ;
		
		session.ghostBy("/logs").children().debugPrint(); 
	}
}
