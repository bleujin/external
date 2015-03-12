package net.ion.external.domain;

import net.ion.craken.ICSCraken;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;

public class Domain {

	private ReadNode dnode;
	private ReadSession session;
	private ICSCraken ic ;
	private String did;
	private DomainInfo dinfo;
	private DomainData ddata;
	public enum Target {
		SiteCategory{
			public String typeName(){
				return "scat" ;
			}
		}, GalleryCategory {
			public String typeName(){
				return "gcat" ;
			}
		} ;
		public abstract String typeName() ;
		public static Target create(String typeName){
			return "scat".equals(typeName) ? SiteCategory : GalleryCategory ;
		}
	}
	
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
		return addCategory(Target.SiteCategory, scatId, includeSub) ;
	}
	public Domain addGalleryCategory(final String gcatId, final boolean includeSub) {
		return addCategory(Target.GalleryCategory, gcatId, includeSub) ;
	}
	public Domain addCategory(final Target target, final String catId, final boolean includeSub) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/", did, target.typeName(), catId).property("includesub", includeSub) ;
				return null;
			}
		}) ;
		return this ;
	}
	
	
	
	public Domain removeSiteCategory(final String scatId) {
		return removeCategory(Target.SiteCategory, scatId) ;
	}
	public Domain removeGalleryCategory(final String gcatId) {
		return removeCategory(Target.GalleryCategory, gcatId) ;
	}
	public Domain removeCategory(final Target target, final String catId) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/domain/", did, target.typeName(), catId).removeSelf() ;
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
