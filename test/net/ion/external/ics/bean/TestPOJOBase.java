package net.ion.external.ics.bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.Article;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.GalleryX;

public class TestPOJOBase extends TestCase {

	protected ICSCraken rc;
	protected ReadSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.rc = ICSCraken.test() ;
		this.session = rc.session() ;
		
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				
				wsession.pathBy("/scat/sales").property("catid", "sales").property("catnm", "sales team").property("parent", "dept");
				wsession.pathBy("/scat/dev").property("catid", "dev").property("catnm", "develop team").refTos("tree", "/scat/research", "/scat/cxm").property("parent", "dept") ;

				wsession.pathBy("/scat/research").property("catid", "reserch").property("deptno", "research team").property("parent", "dev");
				wsession.pathBy("/scat/cxm").property("catid", "cxm").property("catnm", "cxm team").property("parent", "dev");
				
				wsession.pathBy("/scat/dept").property("catid", "dept").property("catnm", "dept root").property("parent", "").refTos("tree", "/scat/sales", "/scat/dev");

				wsession.pathBy("/gcat/idol").property("catid", "idol").property("catnm", "IDOL Group").property("parent", "").refTos("tree", "/gcat/girlsday", "/gcat/kara") ;
				wsession.pathBy("/gcat/girlsday").property("catid", "girlsday").property("catnm", "girls day").property("parent", "idol") ;
				wsession.pathBy("/gcat/kara").property("catid", "kara").property("catnm", "KARA").property("parent", "idol") ;
				
				InputStream data = new FileInputStream("./resource/log4j.properties");
				wsession.pathBy("/article/cxm/7756").property("artid", 7756).property("catid", "cxm").property("priority", 1).property("userid", "bleujin").property("age", 20).property("artfilenm", "my7756.html")
					.property("isusingurlloc", true)
					.blob("data", data) ;
				wsession.pathBy("/article/cxm/7789").property("artid", 7789).property("catid", "cxm").property("priority", 2).property("userid", "hero").property("age", 21) ;
				wsession.pathBy("/article/cxm/7801").property("artid", 7801).property("catid", "cxm").property("priority", 3).property("userid", "airkjh").property("age", 22) ;
				wsession.pathBy("/article/dev/7901").property("artid", 7801).property("catid", "dev").property("priority", 1).property("userid", "novision").property("age", 23) ;
				
				
				wsession.pathBy("/article/cxm/7756").refTos(Article.Related, "/article/cxm/7789", "/article/dev/7901") ;
				
				wsession.pathBy("/gallery/girlsday/10521").property(Def.Gallery.CatId, "girlsday").property(Def.Gallery.GalId, 10521).property(Def.Gallery.TypeCd, "jpg")
					.property(Def.Gallery.FileNm, "0212111300413_0.jpg").property(Def.Gallery.Subject, "minA").blob("data", new FileInputStream("./resource/mina.jpg")) ;
				
				wsession.pathBy("/user/bleujin").property("userid", "bleujin").encrypt(Def.User.VerifyKey, "redf").property(Def.User.EnroleDay, "20100101").property(Def.User.RetireDay, "99991231") ;
				wsession.pathBy("/user/hero").property("userid", "hero").encrypt(Def.User.VerifyKey, "redf").property(Def.User.EnroleDay, "20100101").property(Def.User.RetireDay, "99991231") ;
				
				
				wsession.pathBy("/template/cxm/22402")
					.property("tplid", 22402).property("catid", "cxm").property("listfilenm", "test.html").property("tplkindcd", "list").property("tpltypecd", "HTML").property("tplnm", "list template") ;
				wsession.pathBy("/template/cxm/17025")
					.property("tplid", 17025).property("catid", "cxm").property("listfilenm", "test2.html").property("tplkindcd", "list").property("tpltypecd", "HTML").property("tplnm", "list template2") ;
				wsession.pathBy("/template/cxm/20245")
					.property("tplid", 20245).property("catid", "cxm").property("listfilenm", "").property("tplkindcd", "story").property("tpltypecd", "HTML").property("tplnm", "list template2") ;
				
				
				wsession.pathBy("/afield/lyn_date").property("afieldid", "lyn_date").property("afieldnm", "date").property("typecd", "Date") ;
				wsession.pathBy("/afield/year").property("afieldid", "year").property("afieldnm", "Year").property("typecd", "String") ;
				wsession.pathBy("/afield/month").property("afieldid", "month").property("afieldnm", "Month").property("typecd", "String") ;
				
				
				return null;
			}
		}) ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		rc.unload(); 
		super.tearDown();
	}
	
	public void xtestFirst() throws Exception {
		session.pathBy("/article").walkChildren().debugPrint();
	}

	public ArticleX findArticle(String catId, int artId) throws IOException {
		return rc.findSiteCategory(catId).article(artId);
	}

	public GalleryX findGallery(String catId, int galId) throws IOException {
		return rc.findGalleryCategory(catId).gallery(galId);
	}
	
	
}
