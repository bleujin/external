package net.ion.external.ics.web.domain;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.script.ScriptException;
import javax.ws.rs.core.MultivaluedMap;

import net.ion.cms.rest.sync.Def;
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
import net.ion.external.domain.DomainSub;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.db.manager.script.IdString;
import net.ion.framework.db.manager.script.InstantJavaScript;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.framework.db.manager.script.ResultHandler;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.schedule.AtTime;
import net.ion.framework.schedule.Job;
import net.ion.framework.schedule.NScheduler;
import net.ion.framework.schedule.ScheduledRunnable;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

public class DomainEntry implements Closeable{

	private DomainSub dsub;
	private NScheduler scheduler;
	public final static String EntryName = "dentry" ;
	
	public DomainEntry(DomainSub dsub) throws IOException {
		this.dsub = dsub ;
		initCDDHandler() ;
	}

	private void initCDDHandler() throws IOException {
		ReadSession session = dsub.craken().login() ;

		this.scheduler = new NScheduler("scripter", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("scripters-thread-%d")));
		final JScriptEngine jsengine = JScriptEngine.create();


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
				String jobId = rmap.get("sid");
				EventPropertyReadable rnode = new EventPropertyReadable(cevent);
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
							}, writer, rsession, params, DomainEntry.this, jsengine);

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
							rsession.tran(DomainEntry.end(scriptId, execResult[0], execResult[1])) ;
						} catch (IOException ex) {
							rsession.tran(DomainEntry.end(scriptId, "schedule fail", ex.getMessage())) ; 
						} catch(ScriptException ex){
							rsession.tran(DomainEntry.end(scriptId, "schedule fail", ex.getMessage())) ;
						} finally {
							IOUtil.close(jwriter);
						}
						// write log

					}
				};
			}


		});
		
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public TransactionJob<Void> deleted(Map<String, String> rmap, CDDRemovedEvent cevent) {
				String jobId = "$" + rmap.get("sid");
				scheduler.removeJob(jobId);
				
				return null;
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> rmap, CDDModifiedEvent cevent) {
				final String sid = rmap.get("sid");
				String jobId = "$" + sid;
				
				EventPropertyReadable rnode = new EventPropertyReadable(cevent);
				if (rnode.property(Def.Schedule.ENABLE).asBoolean()) {
					ReadNode sinfo = rsession.ghostBy("/icommands/" + sid + "/schedule");
					scheduler.removeJob(jobId);
					AtTime at = makeAtTime(sinfo);
					scheduler.addJob(new Job(jobId, makeCallable(sid), at));
				} else {
					scheduler.removeJob(jobId);
				}
				
				
				return null;
			}


			private ScheduledRunnable makeCallable(final String sid){
				return new ScheduledRunnable() {
					@Override
					public void run() {
						final ReadNode icommandNode = rsession.ghostBy("/icommands/" + sid);
						final String scriptContent = icommandNode.property("content").asString() ;
						if (StringUtil.isBlank(scriptContent)) return ;
						
						rsession.tran(new TransactionJob<Void>() { // fire event at domainReal
							@Override
							public Void handle(WriteSession wsession) throws Exception {
								WriteNode wnode = wsession.pathBy("/icommands", sid, "run") ;
								wnode.property("scriptid", sid).property("content", scriptContent).increase("count") ;
								return null;
							}
						}) ;
					}
				};
			}

			@Override
			public String pathPattern() {
				return "/icommands/{sid}/schedule";
			}
		}) ;
		
		scheduler.start(); 
		
		// register scripts schedule job
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
				
				IteratorList<WriteNode> icommands = wsession.pathBy("/icommands").children().iterator() ;
				while(icommands.hasNext()){
					WriteNode wnode = icommands.next() ;
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

	public static DomainEntry test(DomainSub dsub) throws IOException {
		DomainEntry result = new DomainEntry(dsub);
		return result;
	}

	@Override
	public void close() throws IOException {
		dsub.craken().stop() ;
	}

	public DomainSub dsub() {
		return dsub;
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
