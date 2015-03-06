package net.ion.external.domain;

import java.io.IOException;

import net.ion.craken.Craken;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.GalleryCategoryX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.TemplateChildrenX;
import net.ion.nsearcher.search.filter.TermFilter;

public class Domain {

	private ReadNode dnode;
	private ReadSession session;
	private Craken ic ;
	private String did;
	
	private Domain(Craken ic, ReadNode dnode) {
		this.dnode = dnode;
		this.session = dnode.session() ;
		this.ic = ic ;
		this.did = dnode.fqn().name() ;
	}

	public static Domain by(Craken ic, ReadNode dnode) {
		return new Domain(ic, dnode);
	}

	public Domain addSiteCategory(final String scatId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/command/domain/addcategory").property("catid", scatId).property("includesub", includeSub).property("did", did) ;
				return null;
			}
		}) ;
		return this ;
	}
	
	public Domain addGallery(final String gcatId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/command/domain/addgallery").property("catid", gcatId).property("includesub", includeSub).property("did", did) ;
				return null;
			}
		}) ;
		return this ;
	}

	public CategoryChildrenX<SiteCategoryX> categorys() throws IOException {
		return CategoryChildrenX.siteCategory(this, session.ghostBy(dnode.fqn()).refsToMe("include").fqnFilter("/datas/scat")) ;
	}

	public CategoryChildrenX<GalleryCategoryX> gcategorys() throws IOException {
		return CategoryChildrenX.galleryCategory(this, session.ghostBy(dnode.fqn()).refsToMe("include").fqnFilter("/datas/gcat")) ;
	}
	
	public ArticleChildrenX articles() throws IOException {
		// /datas/article/{catid}/{artid}
		return ArticleChildrenX.create(this, session.ghostBy(dnode.fqn()).refsToMe("include").fqnFilter("/datas/article"));
	}
	
	public ArticleChildrenX articles(String catId) throws IOException {
		return ArticleChildrenX.create(this, session.ghostBy(dnode.fqn()).refsToMe("include").fqnFilter("/datas/article").filter(new TermFilter("catid", catId)));
	}

	public TemplateChildrenX templates() throws IOException {
		return TemplateChildrenX.create(this, session.ghostBy(dnode.fqn()).refsToMe("include").fqnFilter("/datas/template")) ;
	}

	public ArticleX article(String catId, int artId) {
		return ArticleX.create(this, session.ghostBy("/datas/article/" + catId + "/" + artId));
	}

	
	public String getId() {
		return did;
	}

	public ReadSession session() {
		return session;
	}

	public SiteCategoryX scategory(String catId) {
		return SiteCategoryX.create(this, session.ghostBy("/datas/scat", catId));
	}

	public GalleryCategoryX gcategory(String catId) {
		return GalleryCategoryX.create(this, session.ghostBy("/datas/gcat", catId));
	}
}
