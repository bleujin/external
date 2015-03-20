package net.ion.external.ics.web;

import java.io.File;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.external.ICSSubCraken;
import net.ion.external.domain.DomainSampleMaster;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.web.domain.DomainEntry;
import net.ion.external.ics.web.domain.DomainWeb;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.nradon.stub.StubHttpResponse;
import net.ion.radon.client.StubServer;

public class TestDomainWeb extends TestCase {

	private StubServer ss;
	private OracleDBManager dbm;
	private ICSSubCraken icraken;
	private DomainSub dsub;
    private ReadSession session;


    @Override
	protected void setUp() throws Exception {
		super.setUp();
		this.ss = StubServer.create(DomainWeb.class);
		
		this.dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
		
		this.icraken = ICSSubCraken.test() ;
		DomainSampleMaster dmaster = DomainSampleMaster.create(dbm, icraken)
					.artImageRoot(new File("./resource/uploadfiles/artimage"))
					.galleryRoot(new File("./resource/uploadfiles/gallery"))
					.afieldFileRoot(new File("./resource/uploadfiles/afieldfile"));
		
		this.dsub = DomainSub.create(icraken) ;
		DomainEntry dentry = DomainEntry.test(dsub);

		ss.treeContext().putAttribute("dentry", dentry) ;
        this.session = icraken.login() ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		icraken.stop();
		dbm.destroyPool(null);
		super.tearDown();
	}
	
	public void testDomainList() throws Exception {
		assertEquals("[]", ss.request("/domain/list").get().contentsString());
		
		this.dsub.createDomain("zzz");
		
		StubHttpResponse response = ss.request("/domain/list").get() ;
		assertEquals("[\"zzz\"]", response.contentsString());
	}
	
	public void testCreateDomain() throws Exception {
		StubHttpResponse response = ss.request("/domain/newdomain").post() ;

		assertEquals("newdomain created", response.contentsString()) ;
		assertEquals("/domain/newdomain", this.dsub.findDomain("newdomain").domainNode().fqn().toString()) ;
	}
	
	public void testInfo() throws Exception {
		dsub.findDomain("zzz").addSiteCategory("dynamic", false) ;
		dsub.findDomain("zzz").addGalleryCategory("aaaa", true) ;

		StubHttpResponse response = ss.request("/domain/zzz/info").get();
		response.debugPrint();
		
		JsonObject json = JsonObject.fromString(response.contentsString());
		
		JsonArray scats = json.asJsonArray("scats") ;
		JsonArray gcats = json.asJsonArray("gcats") ;
		assertEquals(1, scats.size()) ;
		assertEquals(1, gcats.size()) ;
		
		JsonObject scat = scats.get(0).getAsJsonObject() ;
		JsonObject gcat = gcats.get(0).getAsJsonObject() ;
		
		assertEquals("dynamic", scat.asString("catid"));
		assertEquals(false, scat.asBoolean("includesub"));
		assertEquals("/host_yucea/0_work/dynamic", scat.asString("catpath"));
		
		assertEquals("aaaa", gcat.asString("catid"));
		assertEquals(true, gcat.asBoolean("includesub"));
		assertEquals("/aaaa", gcat.asString("catpath"));
	}
	
	public void testAddSiteCategory() throws Exception {
		StubHttpResponse response = ss.request("/domain/zzz/define").postParam("catid", "dynamic").postParam("includeSub", "true").postParam("target", "scat").post();

		assertEquals("dynamic created", response.contentsString()) ;
		assertEquals(true, dsub.findDomain("zzz").datas().scategory("dynamic").exists()) ;
	}
	
	public void testAddGalleryCategory() throws Exception {
        StubHttpResponse response = ss.request("/domain/zzz/define").postParam("catid", "aaaa").postParam("includeSub", "true").postParam("target", "gcat").post();

		assertEquals("aaaa created", response.contentsString()) ;
		
		assertEquals(true, dsub.findDomain("zzz").datas().gcategory("aaaa").exists()) ;
		assertEquals(true, dsub.findDomain("zzz").datas().gcategory("bbbb").exists()) ;
	}
	
	public void testRemoveSiteCategory() throws Exception {
		StubHttpResponse response = ss.request("/domain/zzz/define").postParam("catid", "dynamic").postParam("target", "scat").delete();
		
		assertEquals("dynamic removed", response.contentsString()) ;
	}

    public void testMultipleCategory() throws Exception {
        this.dsub.createDomain("zzz");
        this.dsub.findDomain("zzz").addSiteCategory("abcd", false).addSiteCategory("def", false) ;

        StubHttpResponse response = ss.request("/domain/zzz/define").postParam("catid", "abcd,def").postParam("target", "scat").delete();
        assertEquals("abcd,def removed", response.contentsString()) ;
        assertEquals(true, session.ghostBy("/domain/zzz/scat/abcd").isGhost());
        assertEquals(true, session.ghostBy("/domain/zzz/scat/def").isGhost());
    }
}
