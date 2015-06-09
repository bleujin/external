package net.ion.external.domain;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import net.ion.cms.env.ICSFileSystem;
import net.ion.cms.env.SQLLoader;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.tree.Fqn;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.bean.handlers.ScalarHandler;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.StringUtil;

import org.apache.log4j.Logger;

public class MirrorReal implements IMirror{

	private ReadSession localSession;
	private ReadSession icsSession;
	private DBController idc;
	private SQLLoader sloader;
	private ICSFileSystem icsfs;
	private Logger logger = Logger.getLogger(DomainReal.class);

	public void init(ReadSession localSession, ReadSession icsSession, DBController idc) throws IOException {
		this.localSession = localSession ;
		this.icsSession = icsSession ;
		this.idc = idc ;
		this.sloader = SQLLoader.create(getClass().getResourceAsStream("esql.sql"));
	}
	
	
	public void icsFileSystem(ICSFileSystem icsfs){
		this.icsfs = icsfs ;
	}
	
	
	public void content(final int artId) throws SQLException, IOException {
		// index content
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				SQLLoader sloader = MirrorReal.this.sloader ;
				sloader.query(idc, "search_content").addParam("artid", artId) .execHandlerQuery(DomainRSHanlder.contentIndexHandler(wsession)) ;
				sloader.query(idc, "search_content_afield").addParam("artid", artId).execHandlerQuery(DomainRSHanlder.contentAfieldIndexHandler(wsession)) ;
				sloader.query(idc, "search_content_thumbnail").addParam("artid", artId).execHandlerQuery(DomainRSHanlder.contentThumbIndexHandler(wsession)) ;
				
				// ....
				return null;
			}
		}) ;

		article(artId) ;
	}
	
	private void article(int artId) throws SQLException, IOException {
		Rows rows = sloader.query(idc, "article_category").addParam("artId", artId).execQuery() ;
		while(rows.next()){
			article(rows.getString("catId"), rows.getInt("artId")) ;
		}
	}
	
	
	public void article(final String catId, final int artId) throws IOException{
		// index article 
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sloader.query(idc, "search_article").addParam("catid", catId).addParam("artid", artId) .execHandlerQuery(DomainRSHanlder.articleIndexHandler(wsession)) ;
				sloader.query(idc, "search_article_afield").addParam("catid", catId).addParam("artid", artId) .execHandlerQuery(DomainRSHanlder.articlAfieldIndexHandler(wsession)) ;
				sloader.query(idc, "search_article_thumbnail").addParam("catid", catId).addParam("artid", artId).execHandlerQuery(DomainRSHanlder.articleThumbIndexHandler(wsession)) ;
				// ....
				return null;
			}
		}) ;

		final CategoryInfo ci = categoryInfo(catId) ;
		if (ci.isTargeted()){
			
			icsSession.tran(new TransactionJob<Void>() {
				@Override
				public Void handle(final WriteSession wsession) throws Exception {
					// article
					sloader.query(idc, "mirror_article").addParam("catId", catId).addParam("artId", artId).execHandlerQuery(DomainRSHanlder.articleMirrorHandler(wsession, ci.targetFqns())); 
					// article_image
					sloader.query(idc, "mirror_article_image").addParam("catId", catId).addParam("artId", artId).execHandlerQuery(DomainRSHanlder.articleImageMirrorHandler(wsession, icsfs));
					// article_afield
					sloader.query(idc, "mirror_article_afield").addParam("catId", catId).addParam("artId", artId).execHandlerQuery(DomainRSHanlder.articleAfieldMirrorHandler(wsession, icsfs));

					return null;
				}
			}) ;
		}
	}
	
	

	CategoryInfo categoryInfo(String catId) throws IOException {
		
		ReadNode category = icsSession.ghostBy("/datas/scat/" + catId) ;
		if(category.isGhost()) return CategoryInfo.createTarget(catId, ListUtil.EMPTY_LIST) ;

		List<ReadNode> dnodes = category.refs("include").toList() ;
		List<Fqn> domainFqns = ListUtil.newList() ;
		for(ReadNode dnode : dnodes) {
			domainFqns.add(dnode.fqn()) ;
		}

		return CategoryInfo.createTarget(catId, domainFqns);
	}


	public void contentClear(final int artId) {
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				ReadNode found = wsession.readSession().pathBy("/content").childQuery("artid:" + artId).findOne() ;
				if (found != null){
					wsession.pathBy(found.fqn()).removeSelf() ;
				}
				return null;
			}
		}) ;
	}


	public void articleOnly(final String catId, final int artId) throws IOException {

		// index article 
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sloader.query(idc, "search_article").addParam("catid", catId).addParam("artid", artId) .execHandlerQuery(DomainRSHanlder.articleIndexHandler(wsession)) ;
				return null;
			}
		}) ;

		final CategoryInfo ci = categoryInfo(catId) ;
		if (ci.isTargeted()){
			icsSession.tran(new TransactionJob<Void>() {
				@Override
				public Void handle(final WriteSession wsession) throws Exception {
					sloader.query(idc, "mirror_article").addParam("catId", catId).addParam("artId", artId).execHandlerQuery(DomainRSHanlder.articleMirrorHandler(wsession, ci.targetFqns())); 
					return null;
				}
			}) ;
		}
	}


	public void articleClear(final String catId, final int artId) {
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/article", catId, artId).removeSelf() ;
				return null;
			}
		}) ;
		
		icsSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				final CategoryInfo ci = categoryInfo(catId) ;
				String[] fqns = ci.targetFqns() ;
				wsession.pathBy("/datas/article", catId, artId).unRefTos("include", fqns) ;
				return null;
			}
		}) ;
		
	}


	public void gallery(final int galId, final String regUserId, final GInfo gi, final GalleryBean ginfo) {
		icsSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
                WriteNode gnode = wsession.pathBy("/datas/gallery/", gi.gcatId(), galId);
                String subject = gnode.property(Def.Gallery.Subject).asString() ;
                String content = gnode.property(Def.Gallery.Content).asString() ;

                gnode.clear() ;
                gnode.property("galid", galId).property("catid", gi.gcatId())
					.property("subject", subject).property("content", content).property("filename", ginfo.fileName()).property("width", ginfo.width()).property("height", ginfo.height())
                    .property("typecd", ginfo.imgType()).property("reguserid", regUserId).property("regday", DateUtil.currentSeoulToString()).property("filesize", ginfo.size())
					.blob("data", ginfo.asResourceStream())
					.refTos("include", gi.targetFqns());
				return null;
			}
		}) ;
	}


	public void galleryInfo(final GInfo gi, final int galId, final String fileNm, final String subject, final String content, final String fileMeta, final String userId) {
		icsSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/datas/gallery/", gi.gcatId(), galId)
					.property("filename", fileNm).property("subject", subject).property("content", content).property("filemeta", fileMeta).property("moduserid", userId);
				return null;
			}
		}) ;
	}


	public boolean galleryClear(String galIds) throws IOException {
		String[] gids = StringUtil.split(galIds, ",") ;
		final List<Fqn> fqns = icsSession.ghostBy("/datas/gallery").childQuery("", true).in("galid", gids).find().toFqns();

		icsSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				for(Fqn fqn : fqns){
					wsession.pathBy(fqn).removeSelf() ;
				}
				// TODO Auto-generated method stub
				return null;
			}
		}) ;
		return fqns.size() > 0 ;
	}


	public void query(String query) throws SQLException {
		idc.createUserCommand(query).execQuery().debugPrint();
	}


	public String galPath(int galId) throws SQLException {
		return sloader.query(idc, "gallery_catpath").addParam("galId", galId).execHandlerQuery(new ScalarHandler<String>(1));
	}


	public void call(String methodName, Object[] params) throws IOException {
		for (Method m : getClass().getDeclaredMethods()){
			if (m.getName().equals(methodName)){
				try {
					m.invoke(this, params) ;
				} catch (IllegalAccessException e) {
					throw new IOException(e) ;
				} catch (IllegalArgumentException e) {
					throw new IOException(e) ;
				} catch (InvocationTargetException e) {
					throw new IOException(e) ;
				}
			}
		}
	}
}


class CategoryInfo {
	
	private boolean isTargeted;
	private String catId;
	private List<Fqn> domainFqns;

	private CategoryInfo(String catId, List<Fqn> domainFqns) {
		this.catId = catId ;
		this.isTargeted = domainFqns.size() > 0 ;
		this.domainFqns = domainFqns ;
	}
	
	public String[] targetFqns() {
		List<String> result = ListUtil.newList() ;
		for (Fqn fqn : domainFqns) {
			result.add(fqn.toString()) ;
		}
		return result.toArray(new String[0]);
	}

	public boolean isTargeted() {
		return isTargeted ;
	}
	
	public String catId() {
		return catId ;
	}
	
	public List<Fqn> domainFqns() {
		return domainFqns;
	}

	public static CategoryInfo createTarget(String catId, List<Fqn> domainFqns) {
		return new CategoryInfo(catId, domainFqns);
	}

}

class GInfo {

	private boolean isTargeted;
	private int galId;
	private String gcatId;
	private String[] gcats;
	private List<Fqn> taragetFqns;

	private GInfo(boolean isTargeted, int galId, String gcatId, String[] gcats, List<Fqn> targetFqns) {
		this.isTargeted = isTargeted;
		this.galId = galId ;
		this.gcatId = gcatId ;
		this.gcats = gcats ;
		this.taragetFqns = targetFqns ;
	}

	public String[] targetFqns() {
		List<String> result = ListUtil.newList() ;
		for (Fqn fqn : taragetFqns) {
			result.add(fqn.toString()) ;
		}
		return result.toArray(new String[0]);
	}

	public static GInfo createTarget(int galId, String gcatId, String[] gcats, List<Fqn> fqns) {
		return new GInfo(fqns.size() > 0, galId, gcatId, gcats, fqns);
	}
	
	public boolean isTargeted(){
		return isTargeted ;
	}
	
	public String gcatId(){
		return gcatId ;
	}
	
	public int galId(){
		return galId ;
	}

}


class GalleryBean {

	private String fileName;
	private InputStream resource;
	private String imgType;
	private int height;
	private int width;
    private int size;

	public GalleryBean(String fileName, InputStream resource, String imgType, int height, int width, int size){
		this.fileName = fileName ;
		this.resource = resource ;
		this.imgType = imgType ;
		this.height = height ;
		this.width = width ;
        this.size = size ;
	}

	public String fileName() {
		return fileName;
	}

	public InputStream asResourceStream() {
		return resource;
	}

	public String imgType() {
		return imgType;
	}

	public int height() {
		return height;
	}

	public int width() {
		return width;
	}

    public int size() {
        return size;
    }
}

