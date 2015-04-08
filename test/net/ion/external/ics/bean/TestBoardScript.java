package net.ion.external.ics.bean;

import java.io.FileInputStream;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.sun.crypto.provider.RSACipher;

public class TestBoardScript extends TestCase{
	

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

	public void testSessionList() throws Exception {

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				for (int i = 0; i < 5; i++) {
					wsession.pathBy("/sample/board/" + i).property("num", i).property("title", "title " + i).property("content", "content " + i) ;
				}
				return null;
			}
		}) ;
		
		
		StringResourceRepository repo = new StringResourceRepositoryImpl();
		repo.putStringResource("/path/board.vm", IOUtil.toStringWithClose(new FileInputStream("./resource/article.template/simple_board.template")));
		
		VelocityEngine ve = newStringEngine("my.repo", repo);
		assertEquals(true, ve.resourceExists("/path/board.vm")) ;
		
		Template t = ve.getTemplate("/path/board.vm");
		StringWriter sw = new StringWriter();
		VelocityContext vc = new VelocityContext();
		vc.put("session", this.session) ;
		t.merge(vc, sw);
		Debug.line(sw);
	}
	
	private VelocityEngine newStringEngine(String repoName, StringResourceRepository repo) {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(Velocity.RESOURCE_LOADER, "string");
		engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		engine.addProperty("string.resource.loader.repository.name", repoName);
		engine.addProperty("string.resource.loader.repository.static", "false");
		engine.addProperty("string.resource.loader.modificationCheckInterval", "1");
		engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());

		engine.setApplicationAttribute(repoName, repo);
		return engine;
	}

}
