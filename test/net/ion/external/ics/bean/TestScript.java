package net.ion.external.ics.bean;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.external.domain.TestBaseDomain;
import net.ion.framework.util.Debug;

public class TestScript extends TestCase {

	private RepositoryImpl r;
	private ReadSession session;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.r = RepositoryImpl.inmemoryCreateWithTest();
		this.session = r.login("test");
	}
	
	@Override
	protected void tearDown() throws Exception {
		r.shutdown();
		super.tearDown();
	}

	public void testScript() throws Exception {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				for (int i = 0; i < 5; i++) {
					wsession.pathBy("/seq").increase("count");
				}
				return null;
			}
		});

		Debug.line(session.pathBy("/seq").property("count").asInt());
	}
}
