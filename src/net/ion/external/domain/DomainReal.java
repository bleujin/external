package net.ion.external.domain;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.script.ScriptException;
import javax.ws.rs.core.MultivaluedMap;

import net.ion.cms.env.ICSFileSystem;
import net.ion.cms.env.SQLLoader;
import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.cms.rest.sync.Def.ICommandLog;
import net.ion.cms.rest.sync.Def.SLog;
import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.Fqn;
import net.ion.craken.tree.PropertyId;
import net.ion.craken.tree.PropertyValue;
import net.ion.framework.db.DBController;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.manager.script.IdString;
import net.ion.framework.db.manager.script.InstantJavaScript;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.framework.db.manager.script.ResultHandler;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.schedule.AtTime;
import net.ion.framework.schedule.Job;
import net.ion.framework.schedule.ScheduledRunnable;
import net.ion.framework.schedule.NScheduler;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import org.apache.log4j.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

public class DomainReal {
	
	private ReadSession session;
	private DBController idc;
	private ICSFileSystem icsfs;
	private SQLLoader sqlLoader;
	private File galleryRoot;
	private Logger logger = Logger.getLogger(DomainReal.class);
	private NScheduler scheduler ;

	public void init(final ReadSession session, final IContext icontext, DBController idc, ICSFileSystem icsfs, File galleryRoot) throws IOException {
	
		this.session = session ;
		this.idc = idc ;
		this.icsfs = icsfs ;
		this.sqlLoader = SQLLoader.create(getClass().getResourceAsStream("esql.sql"));
		this.galleryRoot = galleryRoot ;
		this.scheduler = new NScheduler("scripter", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("scripters-thread-%d")));

		final JScriptEngine jsengine = JScriptEngine.create();
		
		
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
		
		// when add icommand
		final InstantScript iscript = InstantScript.create() ;
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/icommands/{sid}/run";
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> resolveMap, CDDModifiedEvent cevent) {
				final String action = "set icommand";
				final String sid = resolveMap.get("sid");

				if (StringUtil.isNotBlank(cevent.property(ICommandLog.Status).asString())) return null ;
				
				final String content = cevent.property("content").asString() ;
				final int count = cevent.property("count").asInt() ;
				final Map<String, String> params = MapUtil.newMap() ;
				for(Entry<PropertyId, PropertyValue> entry : cevent.getValue().entrySet()){
					if (entry.getKey().idString().startsWith("param_")) {
						params.put(StringUtil.substringAfter(entry.getKey().idString(), "param_"), entry.getValue().asString()) ;
					}
				}
				
				final StringWriter swriter = new StringWriter() ;
				final AtomicReference<String> status = new AtomicReference<String>() ;
				try {
					iscript.run(content, swriter, session, icontext, params) ;
					swriter.append("completed") ;
					status.set("success");
				} catch (NoSuchMethodException e) {
					swriter.append(e.getMessage()) ;
					status.set("fail");
				} catch (ScriptException e) {
					swriter.append(e.getMessage()) ;
					status.set("fail");
				}
				
				DomainReal.this.session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						WriteNode wnode = ICommandLog.pathBy(wsession, sid, "c" + (count % 100)) ;
						wnode.property(ICommandLog.Content, content) ;
						for (Entry<String, String> entry : params.entrySet()) {
							wnode.property("param_" + entry.getKey(), entry.getValue()) ;
						}
						wnode.property(ICommandLog.Result, swriter.toString()).property(ICommandLog.Status, status.get()).property(ICommandLog.Runtime, System.currentTimeMillis()) ;
						return null;
					}
				}) ;
				return null ;
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> resolveMap, CDDRemovedEvent cevent) {
				return null;
			}
		});
		
		
		
		// when mod scripts schedule
		final ReadSession rsession = session ;
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/scripts/{sid}/schedule";
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> rmap, CDDRemovedEvent cevent) {
				String sid = rmap.get("sid");
				scheduler.removeJob(sid);
				
				return null;
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> rmap, CDDModifiedEvent cevent) {
				EventPropertyReadable rnode = new EventPropertyReadable(cevent);
				String jobId = rmap.get("sid");
				if (rnode.property(Def.Schedule.ENABLE).asBoolean()) {
					ReadNode sinfo = rsession.ghostBy("/scripts/" + jobId + "/schedule");
					scheduler.removeJob(jobId);
					AtTime at = makeAtTime(sinfo);
					scheduler.addJob(new Job(jobId, makeCallable(jobId), at));
				} else {
					scheduler.removeJob(jobId);
				}

				return null;
			}

			private ScheduledRunnable makeCallable(final String scriptId) {
				return new ScheduledRunnable() {
					@Override
					public void run() {
						final ReadNode scriptNode = rsession.ghostBy("/scripts/" + scriptId);

						// should check running(in distribute mode)
						if (scriptNode.property(Def.Script.Running).asBoolean()) return ;
						rsession.tran(new TransactionJob<Void>() {
							@Override
							public Void handle(WriteSession wsession) throws Exception {
								wsession.pathBy(scriptNode.fqn()).property(Def.Script.Running, true) ;
								return null;
							}
						}) ;
						// 
						
						
						
						String scriptContent = scriptNode.property(Def.Script.Content).asString();
						StringWriter result = new StringWriter();
						final JsonWriter jwriter = new JsonWriter(result);

						
						
						try {
							StringWriter writer = new StringWriter();
							MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
							InstantJavaScript script = jsengine.createScript(IdString.create(scriptId), "", new StringReader(scriptContent));

							String[] execResult = script.exec(new ResultHandler<String[]>() {
								@Override
								public String[] onSuccess(Object result, Object... args) {
									try {
										jwriter.beginObject().name("return").value(ObjectUtil.toString(result));
									} catch (IOException ignore) {
									} finally {
										rsession.tran(new TransactionJob<Void>() {
											@Override
											public Void handle(WriteSession wsession) throws Exception {
												wsession.pathBy(scriptNode.fqn()).property(Def.Script.Running, false) ;
												return null;
											}
										}) ;
									}
									return new String[]{"schedule success", ObjectUtil.toString(result)};
								}

								@Override
								public String[] onFail(Exception ex, Object... args) {
									try {
										jwriter.beginObject().name("return").value("").name("exception").value(ex.getMessage());
									} catch (IOException e) {
									} finally {
										rsession.tran(new TransactionJob<Void>() {
											@Override
											public Void handle(WriteSession wsession) throws Exception {
												wsession.pathBy(scriptNode.fqn()).property(Def.Script.Running, false) ;
												return null;
											}
										}) ;
									}
									return new String[]{"schedule fail", ex.getMessage()};
								}
							}, writer, rsession, params, DomainReal.this, jsengine);

							jwriter.name("writer").value(writer.toString());

							jwriter.name("params");
							jwriter.beginArray();
							for (Entry<String, List<String>> entry : params.entrySet()) {
								jwriter.beginObject().name(entry.getKey()).beginArray();
								for (String val : entry.getValue()) {
									jwriter.value(val);
								}
								jwriter.endArray().endObject();
							}
							jwriter.endArray();
							jwriter.endObject();
							jwriter.close();
							rsession.tran(DomainReal.end(scriptId, execResult[0], execResult[1])) ;
						} catch (IOException ex) {
							rsession.tran(DomainReal.end(scriptId, "schedule fail", ex.getMessage())) ; 
						} catch(ScriptException ex){
							rsession.tran(DomainReal.end(scriptId, "schedule fail", ex.getMessage())) ;
						} finally {
							IOUtil.close(jwriter);
						}
						// write log

					}
				};
			}

			private AtTime makeAtTime(ReadNode sinfo) {
				String expr = StringUtil.coalesce(sinfo.property("minute").asString(), "*") + " " 
						+ StringUtil.coalesce(sinfo.property("hour").asString(), "*") + " " 
						+ StringUtil.coalesce(sinfo.property("day").asString(), "*") + " " 
						+ StringUtil.coalesce(sinfo.property("month").asString(), "*") + " " 
						+ StringUtil.coalesce(sinfo.property("week").asString(), "*") + " "
						+ StringUtil.coalesce(sinfo.property("matchtime").asString(), "*") + " " 
						+ StringUtil.coalesce(sinfo.property("year").asString(), "*");

				return new AtTime(expr);
			}

		});
		
		scheduler.start(); 
		
		// register schedule job
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				IteratorList<WriteNode> scripts = wsession.pathBy("/scripts").children().iterator() ;
				while(scripts.hasNext()){
					WriteNode wnode = scripts.next() ;
					wnode.property(Def.Script.Running, false) ;
					if (wnode.hasChild("schedule")){
						WriteNode scheduleNode = wnode.child("schedule");
						if (scheduleNode.property(Def.Schedule.ENABLE).asBoolean()){
							scheduleNode.property(Def.Schedule.ENABLE, true) ;
						}
					}
				}
				return null;
			}
		}) ;
	}
	
	private static TransactionJob<Void> end(final String sid, final String status, final Object result){
		return new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode logNode = wsession.pathBy(SLog.path(sid));
				long cindex = logNode.property(SLog.CIndex).asLong(0) ;
				logNode.child("c" + cindex).property(SLog.Runtime, System.currentTimeMillis()).property(SLog.Status, status).property(SLog.Result, ObjectUtil.toString(result)) ;
				logNode.property(SLog.CIndex, (++cindex) % 101) ;
				return null;
			}
		} ;
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


interface PropertyReadable {
	public PropertyValue property(String propId);

	public PropertyValue property(PropertyId propId);

	public Fqn fqn();
}

class EventPropertyReadable implements PropertyReadable {

	private CDDModifiedEvent event;

	public EventPropertyReadable(CDDModifiedEvent event) {
		this.event = event;
	}

	@Override
	public PropertyValue property(String propId) {
		return event.property(propId);
	}

	@Override
	public PropertyValue property(PropertyId propId) {
		return event.property(propId);
	}

	public Fqn fqn() {
		return event.getKey().getFqn();
	}
}

class RNodePropertyReadable implements PropertyReadable {

	private ReadNode rnode;

	public RNodePropertyReadable(ReadNode node) {
		this.rnode = node;
	}

	@Override
	public PropertyValue property(String propId) {
		return rnode.property(propId);
	}

	@Override
	public PropertyValue property(PropertyId propId) {
		return rnode.propertyId(propId);
	}

	public Fqn fqn() {
		return rnode.fqn();
	}
}
