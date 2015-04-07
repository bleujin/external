package net.ion.external.domain;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import net.ion.cms.env.ICSFileSystem;
import net.ion.cms.env.SQLLoader;
import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.StringUtil;

public class DomainReal {
	
	private ReadSession session;
	private DBController idc;
	private ICSFileSystem icsfs;
	private SQLLoader sqlLoader;
	private File galleryRoot;
	private Logger logger = Logger.getLogger(DomainReal.class);

	public void init(ReadSession session, DBController idc, ICSFileSystem icsfs, File galleryRoot) throws IOException {
	
		this.session = session ;
		this.idc = idc ;
		this.icsfs = icsfs ;
		this.sqlLoader = SQLLoader.create(getClass().getResourceAsStream("esql.sql"));
		this.galleryRoot = galleryRoot ;

		// when addCategory
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/domain/{did}/scat/{catid}";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String action = "add site category";
				final String did = resolveMap.get("did");
				final String catId = resolveMap.get("catid");
				whenAddCategory(catId, cevent.property("includesub").asBoolean(), did);

				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				final String action = "remove site category";
				final String did = resolveMap.get("did");
				final String catId = resolveMap.get("catid");
				DomainReal.this.session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						ReadNode scatNode = wsession.readSession().ghostBy("/domain", did, "scat", catId);
						IteratorList<ReadNode> iter = scatNode.refsToMe("include").fqnFilter("/datas").find().iterator();
						while (iter.hasNext()) {
							ReadNode node = iter.next();
							wsession.pathBy(node.fqn()).unRefTos("include", scatNode.fqn().toString());
						}
						return null;
					}
				});
				return null;
			}
		});

		// when add Gallery Category
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/domain/{did}/gcat/{catid}";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String action = "add gallery category";
				final String did = resolveMap.get("did");
				final String catId = resolveMap.get("catid");

				whenAddGallery(catId, cevent.property("includesub").asBoolean(), did);
				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				final String action = "remove gallery category";
				final String did = resolveMap.get("did");
				final String catId = resolveMap.get("catid");

				DomainReal.this.session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						ReadNode gcatNode = wsession.readSession().ghostBy("/domain", did, "gcat", catId);
						IteratorList<ReadNode> iter = gcatNode.refsToMe("include").fqnFilter("/datas").find().iterator();
						while (iter.hasNext()) {
							ReadNode node = iter.next();
							wsession.pathBy(node.fqn()).unRefTos("include", gcatNode.fqn().toString());
						}
						return null;
					}
				});

				return null;
			}
		});

		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/domain/{did}";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> map, CDDModifiedEvent cddmodifiedevent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> map, CDDRemovedEvent cddremovedevent) {
				// TODO Auto-generated method stub
				return null;
			}
		});

		// etc
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/command/domain/{action}";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String action = resolveMap.get("action");

				if ("resetuser".equals(action)) {
					whenResetUser();
				}

				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		
		
	}
	
	

	private void whenResetUser() {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "user").execHandlerQuery(new ResultSetHandler<Void>() {
					public Void handle(ResultSet rs) throws SQLException {
						while (rs.next()) {
							if (wsession.exists(rs.getString("fqn")))
								continue;

							WriteNode wnode = wsession.pathBy(rs.getString("fqn"));
							Def.User.Properties(wnode, rs);
						}
						return null;
					}
				});
				return null;
			}
		});
	}

	private void whenAddCategory(final String catId, final Boolean includeSub, final String did) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				// category
				sqlLoader.query(idc, "category").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, new String[]{"/domain/" + did + "/scat/" + catId}, Def.Category.class, "orderLnNo", ""));

				// category_afield
				sqlLoader.query(idc, "category_afield").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
						while (rs.next()) {
							wsession.pathBy(rs.getString("fqn")).refTos("afields", rs.getString("tfqn"));
						}
						return null;
					}
				});

				// template
				sqlLoader.query(idc, "template").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, new String[]{"/domain/" + did + "/scat/" + catId}, Def.Template.class, "tplid", ""));

				// afiled
				if (!wsession.exists("/datas/afield")) {
					sqlLoader.query(idc, "afield").execHandlerQuery(new ResultSetHandler<Void>() {
						@Override
						public Void handle(ResultSet rs) throws SQLException {
							while (rs.next()) {
								if (rs.getInt("lvl") > 1) {
									wsession.pathBy(rs.getString("fqn")).refTos("tree", "/datas/afield/" + rs.getString("lowerid"));
								} else {
									WriteNode wnode = wsession.pathBy(rs.getString("fqn"));
									Def.Afield.Properties(wnode, rs);
								}
							}
							return null;
						}
					});
				} ;
				
				// articles
				sqlLoader.query(idc, "articles").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(DomainRSHanlder.articleMirrorHandler(wsession, new String[]{"/domain/" + did + "/scat/" + catId}));

				// articles_afield
				sqlLoader.query(idc, "articles_afield").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(DomainRSHanlder.articleAfieldMirrorHandler(wsession, icsfs));
				
				// articles_image
				sqlLoader.query(idc, "articles_image").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(DomainRSHanlder.articleImageMirrorHandler(wsession, icsfs));

				return null;
			}

		});
	}

	private void whenAddGallery(final String catId, final Boolean includeSub, final String did) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "gcategory").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, new String[]{"/domain/" + did + "/gcat/" + catId}, Def.GalleryCategory.class, "", ""));
				sqlLoader.query(idc, "gallery").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
						int count = 0 ;
						while (rs.next()) {
							WriteNode wnode = wsession.pathBy(rs.getString("fqn"));
							Gallery.Properties(wnode, rs);

							String basePath = StringUtil.leftPad(rs.getString("galid"), 9, '0');
							File findDir = new File(galleryRoot, StringUtil.substring(basePath, 0, 3) + "/" + StringUtil.substring(basePath, 3, 6) + "/" + StringUtil.substring(basePath, 6, 9));
							File resource = new File(findDir, rs.getString("galid"));
							if (resource.exists()) {
								try {
									DataInputStream in = new DataInputStream(new FileInputStream(resource));
									int size = in.readInt();
									in.skipBytes(size);

									wnode.blob("data", in);
								} catch (IOException ignore) {
									logger.warn(ignore.getMessage());
								}
							}
							wnode.refTos("include", "/domain/" + did + "/gcat/" + catId);
							DomainRSHanlder.continueUnit(wsession, count++);
						}
						return null;
					}
				});
				return null;
			}
		});
	}

	private ResultSetHandler<Void> createHandler(final WriteSession wsession, final String[] domainFqns, final Class clz, final String ncols, final String bcols) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(final ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					WriteNode wnode = wsession.pathBy(rs.getString("fqn"));
					for (String col : StringUtil.split(ncols, ",")) {
						wnode.property(col, rs.getInt(col));
					}
					for (String col : StringUtil.split(bcols, ",")) {
						wnode.property(col, rs.getBoolean(col));
					}
					String cols = ncols + "," + bcols;
					String[] dcols = StringUtil.split(cols, ",");
					for (Field field : clz.getDeclaredFields()) {
						String colName = field.getName().toLowerCase();
						if (ArrayUtil.contains(dcols, colName) || "fqn".equals(colName))
							continue;
						wnode.property(colName, rs.getString(colName));
					}
					wnode.refTos("include", domainFqns);
					DomainRSHanlder.continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}



	public void buildLocalIndex(ReadSession localSession, String rebuildContent, String rebuildArticle) throws IOException {
		if ("true".equals(rebuildContent)) {
			Debug.info("start job : content to craken local");
			updateContent(localSession);
			Debug.info("end job : content to craken local");
		}
		if ("true".equals(rebuildArticle)) {
			Debug.info("start job : article to craken local");
			updateArticle(localSession);
			Debug.info("end job : article to craken local");
		}
	}



	public void updateContent(ReadSession localSession) throws IOException {
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "update_content").execHandlerQuery(DomainRSHanlder.contentIndexHandler(wsession)) ;
				sqlLoader.query(idc, "update_content_afield").execHandlerQuery(DomainRSHanlder.contentAfieldIndexHandler(wsession)) ;
				sqlLoader.query(idc, "update_content_thumbnail").execHandlerQuery(DomainRSHanlder.contentThumbIndexHandler(wsession)) ;

				return null;
			}


			
		}) ;
	}

	public void updateArticle(ReadSession localSession) throws IOException {
		localSession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "update_article").execHandlerQuery(DomainRSHanlder.articleIndexHandler(wsession)) ;
				sqlLoader.query(idc, "update_article_afield").execHandlerQuery(DomainRSHanlder.articlAfieldIndexHandler(wsession)) ;
				sqlLoader.query(idc, "update_article_thumbnail").execHandlerQuery(DomainRSHanlder.articleThumbIndexHandler(wsession)) ;

				return null;
			}
			
		}) ;
	}
	
}
