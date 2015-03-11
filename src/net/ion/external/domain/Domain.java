package net.ion.external.domain;

import java.io.IOException;

import oracle.net.aso.s;
import net.ion.craken.ICSCraken;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;
import net.ion.external.ics.bean.AfieldMetaX;
import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.GalleryCategoryX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.TemplateChildrenX;
import net.ion.external.ics.bean.UserX;
import net.ion.external.ics.bean.XIterable;
import net.ion.nsearcher.search.filter.TermFilter;

public class Domain {

	private ReadNode dnode;
	private ReadSession session;
	private ICSCraken ic ;
	private String did;
	private DomainInfo dinfo;
	private DomainData ddata;
	
	private Domain(ICSCraken ic, ReadNode dnode) {
		this.dnode = dnode;
		this.session = dnode.session() ;
		this.ic = ic ;
		this.did = dnode.fqn().name() ;
		this.dinfo = new DomainInfo(this) ;
		this.ddata = new DomainData(this) ;
	}

	public static Domain by(ICSCraken ic, ReadNode dnode) {
		return new Domain(ic, dnode);
	}

	public Domain addSiteCategory(final String scatId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/", did, "scat", scatId).property("includesub", includeSub) ;
				return null;
			}
		}) ;
		return this ;
	}
	
	public Domain removeSiteCategory(final String scatId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/", did, "scat", scatId).removeSelf() ;
				return null;
			}
		}) ;
		return this ;
	}

	
	public Domain addGalleryCategory(final String gcatId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain", did, "gcat", gcatId).property("includesub", includeSub) ;
				return null;
			}
		}) ;
		return this ;
	}
	
	public Domain resetUser() {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				
				wsession.pathBy("/command/domain/resetuser") ;
				return null;
			}
		}) ;
		
		return this ;
	}


	public String getId() {
		return did;
	}

	public ReadSession session() {
		return session;
	}

	
	public DomainInfo info() {
		return this.dinfo;
	}

	public DomainData datas() {
		return ddata;
	}
	
	public ReadNode domainNode() {
		return ghostBy(dnode.fqn());
	}

	ReadNode ghostBy(Fqn fqn) {
		return session.ghostBy(fqn);
	}
	
	ReadNode ghostBy(String base, String... sub) {
		return session.ghostBy(base, sub) ;
	}
}
