package net.ion.external.domain;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.Craken;
import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.ObjectId;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.kr.utils.StringUtil;

public class DomainMaster {

	private DBController idc;
	private Craken ic;
	private SQLLoader sqlLoader;
	private ReadSession session;
	private File artImageDir;
	private File galleryRoot;

	private Logger logger = Logger.getLogger(DomainMaster.class);
	private File afieldRoot;

	public DomainMaster(OracleDBManager dbm, Craken ic) throws IOException {
		this.idc = new DBController(dbm);
		this.ic = ic;
		this.session = ic.login();
		this.sqlLoader = SQLLoader.create(getClass().getResourceAsStream("esql.sql"));
		init();
	}

	private void init() throws IOException {
		final ReadSession session = ic.login();

		// when addCategory
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/domain/{did}/scat/{catid}";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String action = "add site category";
				final String did = resolveMap.get("did") ;
				final String catId = resolveMap.get("catid") ;
				startAction(session, action);
				whenAddCategory(catId, cevent.property("includesub").asBoolean(), did);
				endAction(session, action);
				
				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				// TODO Auto-generated method stub
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
				final String did = resolveMap.get("did") ;
				final String catId = resolveMap.get("catid") ;
				
				startAction(session, action);
				whenAddGallery(catId, cevent.property("includesub").asBoolean(), did);
				
				endAction(session, action);
				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
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
				startAction(session, action);
				
				if ("addcategory".equals(action)) {
					whenAddCategory(cevent.property("catid").asString(), cevent.property("includesub").asBoolean(), cevent.property("did").asString());
				} else if ("addgallery".equals(action)) {
					whenAddGallery(cevent.property("catid").asString(), cevent.property("includesub").asBoolean(), cevent.property("did").asString());
				} else if ("resetuser".equals(action)){
					whenResetUser() ;
				} else if ("removecategory".equals(action)) {
					whenRemoveCategory(cevent.property("catid").asString(), cevent.property("includesub").asBoolean(), cevent.property("did").asString());
				}
				
				endAction(session, action);
				
				return null;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	
	
	

	public static DomainMaster create(OracleDBManager dbm, Craken ic) throws IOException {
		return new DomainMaster(dbm, ic);
	}

	
	
	private void startAction(final ReadSession session, final String action) {
		session.tran(new TransactionJob<Void>() {
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/datas/log", new ObjectId()).property("action", action).property("type", "start").property("time", System.currentTimeMillis()) ;
				return null;
			}
		}) ;
	}

	private void endAction(final ReadSession session, final String action) {
		session.tran(new TransactionJob<Void>() {
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/datas/log", new ObjectId()).property("action", action).property("type", "end").property("time", System.currentTimeMillis()) ;
				return null;
			}
		}) ;
	}


	

	
	
	

	private void whenResetUser() {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "user").execHandlerQuery(new ResultSetHandler<Void>() {
					public Void handle(ResultSet rs) throws SQLException {
						while(rs.next()){
							if (wsession.exists(rs.getString("fqn"))) continue ;
							
							WriteNode wnode = wsession.pathBy(rs.getString("fqn")) ;
							Def.User.Properties(wnode, rs) ;
						}
						return null;
					}
				}) ;
				return null;
			}
		}) ;
	}


	
	private void whenAddCategory(final String catId, final Boolean includeSub, final String did) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				// category
				sqlLoader.query(idc, "category").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, Def.Category.class, did, "orderLnNo", ""));
				
				// category_afield
				sqlLoader.query(idc, "category_afield").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).refTos("afields", rs.getString("tfqn")) ;
						}
						return null;
					}
				});
				

				// article
				sqlLoader.query(idc, "articles").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, Def.Article.class, did, "artid,modserno,orderno,partid,priority", "isusingurlloc"));

				// article_image
				sqlLoader.query(idc, "articles_image").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(final ResultSet rs) throws SQLException {
						while (rs.next()) {
							File file = new File(artImageDir, rs.getString("imgfileloc"));
							if (!file.exists())
								continue;
							try {
								wsession.pathBy(rs.getString("fqn")).blob(rs.getBoolean("isthumbimg") ? "thumbimg" : "img" + rs.getString("imgFileLoc").toLowerCase().hashCode(), new FileInputStream(file));
							} catch (IOException ignore) {
								logger.warn(ignore.getMessage());
							}
						}
						return null;
					}
				});

				// article_afield
				sqlLoader.query(idc, "articles_afield").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
						while (rs.next()) {
							WriteNode articleNode = wsession.pathBy(rs.getString("articleFqn"));
							articleNode.property(rs.getString("afieldid"), rs.getString("dvalue") + rs.getString("clobvalue"));

							WriteNode afieldNode = wsession.pathBy(rs.getString("fqn"));
							Def.AfieldValue.Properties(afieldNode, rs);

							String typeCd = rs.getString("typeCd");
							if (("File".equals(typeCd) || "Image".equals(typeCd)) && StringUtil.isNotBlank(rs.getString("dvalue"))) {
								File file = new File(afieldRoot, rs.getString("dvalue"));
								if (!file.exists())
									continue;
								try {
									afieldNode.blob("data", new FileInputStream(file));
								} catch (IOException ignore) {
									logger.warn(ignore.getMessage());	
								}
							}

						}
						return null;
					}
				});

				// template
				sqlLoader.query(idc, "template").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, Def.Template.class, did, "tplid", ""));

				
				// afiled
				if (! wsession.exists("/datas/afield")){
					sqlLoader.query(idc, "afield").execHandlerQuery(new ResultSetHandler<Void>() {
						@Override
						public Void handle(ResultSet rs) throws SQLException {
							while(rs.next()){
								if (rs.getInt("lvl") > 1){
									wsession.pathBy(rs.getString("fqn")).refTos("tree", "/datas/afield/" + rs.getString("lowerid")) ;
								} else {
									WriteNode wnode = wsession.pathBy(rs.getString("fqn")) ;
									Def.Afield.Properties(wnode, rs) ;
								}
							}
							return null;
						}
					});
				} ;
				
				return null;
			}
		});
	}

	private void whenAddGallery(final String catId, final Boolean includeSub, final String did) {
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(final WriteSession wsession) throws Exception {
				sqlLoader.query(idc, "gcategory").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(createHandler(wsession, Def.GalleryCategory.class, did, "", ""));
				sqlLoader.query(idc, "gallery").addParam("catId", catId).addParam("includeSub", includeSub ? "T" : "F").execHandlerQuery(new ResultSetHandler<Void>() {
					@Override
					public Void handle(ResultSet rs) throws SQLException {
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
						}
						return null;
					}
				});
				return null;
			}
		});
	}

	private ResultSetHandler<Void> createHandler(final WriteSession wsession, final Class clz, final String did, final String ncols, final String bcols) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(final ResultSet rs) throws SQLException {
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
					wnode.refTo("include", "/domain/" + did);
				}
				return null;
			}
		};
	}
	
	private void whenRemoveCategory(String catId, Boolean includeSub, String did) {
		// TODO Auto-generated method stub
		
	}



	public DomainMaster artImageRoot(File path) {
		this.artImageDir = path;
		return this;
	}

	public DomainMaster galleryRoot(File path) {
		this.galleryRoot = path;
		return this;
	}

	public DomainMaster afieldFileRoot(File path) {
		this.afieldRoot = path;
		return this;
	}
}
