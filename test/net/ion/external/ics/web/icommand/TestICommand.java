package net.ion.external.ics.web.icommand;

import java.util.Map;

import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;

public class TestICommand extends TestCase {

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
	
	public void testSelfDetroyCDDHandler() throws Exception {
		session.workspace().cddm().add(new CDDHandler() {
			public String pathPattern() {
				return "/emps/{userid}";
			}
			@Override
			public TransactionJob<Void> modified(Map<String, String> rmap, CDDModifiedEvent cevent) {
				String userId = rmap.get("userid") ;
				Debug.line(userId, session.workspace().cddm());
				
				session.workspace().cddm().remove(this);
				return null;
			}
			@Override
			public TransactionJob<Void> deleted(Map<String, String> arg0, CDDRemovedEvent arg1) {
				return null;
			}
		}) ;
		

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/emps/bleujin").property("name", "bleujin") ;
				return null;
			}
		}).get() ;
		Thread.sleep(1000);

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/emps/hero").property("name", "hero") ;
				return null;
			}
		}) ;
	}
}
