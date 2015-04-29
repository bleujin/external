package net.ion.external.ics.web.icommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.ICommandLog;
import net.ion.cms.rest.sync.Def.SLog;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.PropertyValue;
import net.ion.external.ICSSubCraken;
import net.ion.external.ics.EventSourceEntry;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.util.WebUtil;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.db.manager.script.IdString;
import net.ion.framework.db.manager.script.InstantJavaScript;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.framework.db.manager.script.ResultHandler;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonParser;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectId;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;
import net.ion.radon.core.ContextParam;

import org.apache.ecs.xhtml.script;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;

import com.google.common.base.Function;

@Path("/icommands")
public class ICommandWeb implements Webapp {

	private ReadSession rsession;
	private JScriptEngine jengine;
	private ICSSubCraken rentry;
	private EventSourceEntry esentry;

	public ICommandWeb(@ContextParam(ICSSubCraken.EntryName) ICSSubCraken icraken, @ContextParam("jsentry") JScriptEngine jengine, @ContextParam("esentry") EventSourceEntry esentry) throws IOException {
		this.rentry = icraken;
		this.rsession = icraken.login();
		this.jengine = jengine;
		this.esentry = esentry;
	}

	@Path("")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject listICommand() {
		JsonArray jarray = rsession.ghostBy("/icommands").children().transform(new Function<Iterator<ReadNode>, JsonArray>() {
			@Override
			public JsonArray apply(Iterator<ReadNode> iter) {
				JsonArray result = new JsonArray();
				try {
					while (iter.hasNext()) {
						ReadNode node = iter.next();
						JsonArray userProp = new JsonArray();
						userProp.add(new JsonPrimitive(node.fqn().name()));
						String firstLine = new BufferedReader(new StringReader(node.property(Def.ICommand.Content).asString())).readLine();
						userProp.add(new JsonPrimitive(StringUtil.defaultString(firstLine, "")));
						result.add(userProp);
					}
				} catch (IOException ignore) {
					ignore.printStackTrace();
				}

				return result;
			}
		});
		return new JsonObject().put("info", rsession.ghostBy("/menus/icommand").property("overview").asString()).put("schemaName", JsonParser.fromString("[{'title':'Id'},{'title':'Run Path'},{'title':'Explain'}]").getAsJsonArray()).put("icommands", jarray);
	}

	@Path("/{sid}/overview")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject overview(@PathParam(Def.ICommand.Sid) final String sid) {

		JsonArray clogs = rsession.ghostBy(ICommandLog.path(sid)).children().descending(ICommandLog.Runtime).transform(new Function<Iterator<ReadNode>, JsonArray>() {
			@Override
			public JsonArray apply(Iterator<ReadNode> iter) {
				JsonArray result = new JsonArray();
				while (iter.hasNext()) {
					ReadNode node = iter.next();
					JsonArray userProp = new JsonArray();
					userProp.add(new JsonPrimitive(node.fqn().name()));
					userProp.add(new JsonPrimitive(DateUtil.timeMilliesToDay(node.property(ICommandLog.Runtime).asLong(0))));
					userProp.add(new JsonPrimitive(node.property(ICommandLog.Status).asString()));
					userProp.add(new JsonPrimitive(node.property(ICommandLog.Result).asString()));
					result.add(userProp);
				}

				return result;
			}
		});

		JsonObject result = new JsonObject();
		result.add("slogs", clogs);
		result.put("schemaName", JsonParser.fromString("[{'title':'Id'},{'title':'Run Time'},{'title':'Status'},{'title':'Result'}]").getAsJsonArray());
		result.put("info", rsession.ghostBy("/menus/icommand").property("overview").asString());

		return result;
	}

	@Path("/{sid}")
	@DELETE
	public String removeICommand(@PathParam(Def.ICommand.Sid) final String sid) {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy(ICommandLog.path(sid)).removeSelf();
				WriteNode found = Def.ICommand.pathBy(wsession, sid);
				FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, sid + ".misc.icommand.bak"), found.property(Def.ICommand.Content).asString());
				found.removeSelf();
				return null;
			}
		});
		return sid + " removed";
	}

	protected ReadSession session() {
		return rsession;
	}

	@Path("/{sid}/define")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject viewICommand(@PathParam(Def.ICommand.Sid) final String sid) {
		ReadNode found = Def.ICommand.ghostBy(rsession, sid);
		return new JsonObject().put("sid", found.fqn().name()).put("samples", WebUtil.findICommands()).put("info", rsession.ghostBy("/menus/icommand").property("define").asString()).put("content", found.property("content").asString());
	}

	@Path("/{sid}/define")
	@POST
	public String defineICommand(@PathParam(Def.ICommand.Sid) final String sid, @DefaultValue("") @FormParam(Def.ICommand.Content) final String content) {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode found = Def.ICommand.pathBy(wsession, sid);
				wsession.pathBy("/icommands", sid, "slogs");
				FileUtil.forceWriteUTF8(new File(Webapp.REMOVED_DIR, sid + ".misc.icommand.bak"), found.property(Def.ICommand.Content).asString());

				found.property(Def.ICommand.Content, content);
				return null;
			}
		});
		return sid + " defined";
	}

	@Path("/{sid}/sampleicommand/{fileName}")
	@GET
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String sampleICommand(@PathParam("fileName") String fileName) throws IOException {
		return WebUtil.viewICommand(fileName);
	}

	@POST
	@Path("/{sid}/removes")
	public String removeICommands(@FormParam("icommands") final String icommands) {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				String[] targets = StringUtil.split(icommands, ",");
				for (String icommandid : targets) {
					Def.ICommand.pathBy(wsession, icommandid).removeSelf();
				}
				return null;
			}
		});
		return "removed " + icommands;
	}

	@Path("/{sid}/run")
	@POST
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public Response runICommand(@PathParam(Def.ICommand.Sid) String sid, @Context HttpRequest request) throws IOException, ScriptException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
		for (Entry<String, List<String>> entry : request.getUri().getQueryParameters().entrySet()) {
			if (StringUtil.isNotBlank(entry.getKey()))
				params.put(entry.getKey(), entry.getValue());
		}

		for (Entry<String, List<String>> entry : request.getDecodedFormParameters().entrySet()) {
			if (StringUtil.isNotBlank(entry.getKey()))
				params.put(entry.getKey(), entry.getValue());
		}

		String content = Def.ICommand.ghostBy(rsession, sid).property(Def.ICommand.Content).asString();
		return runICommand(sid, params, content, request.getHttpMethod());
	}

	@Path("/{sid}/instantrun")
	@POST
	public Response instantRunICommand(@PathParam("sid") final String sid, @DefaultValue("") @FormParam("content") String content, @Context HttpRequest request) throws IOException, ScriptException {

		return runICommand(sid, new MultivaluedMapImpl<String, String>(), content, request.getHttpMethod());
		// return sid + " called";
	}

	public static TransactionJob<Void> end(final String sid, final String status, final Object result) {
		return new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode logNode = wsession.pathBy(SLog.path(sid));
				long cindex = logNode.property(SLog.CIndex).asLong(0);
				logNode.child("c" + cindex).property(SLog.Runtime, System.currentTimeMillis()).property(SLog.Status, status).property(SLog.Result, ObjectUtil.toString(result));
				logNode.property(SLog.CIndex, (++cindex) % 101);
				return null;
			}
		};
	}

	private Response runICommand(final String sid, final MultivaluedMap<String, String> params, final String content, final String method) throws IOException, ScriptException {
		try {
			String runId = this.rsession.tran(new TransactionJob<String>() {
				@Override
				public String handle(WriteSession wsession) throws Exception {
					WriteNode wnode = wsession.pathBy("/icommands", sid, "run");

					PropertyValue countValue = wnode.property("scriptid", sid).property("content", content).property("method", method).increase("count");
					String runid = "c" + countValue.asInt();
					wnode.property("runid", runid);
					for (Entry<String, List<String>> entry : params.entrySet()) {
						wnode.property("param_" + entry.getKey(), entry.getValue());
					}
					return runid;
				}
			}).get();

			String resultValue = rsession.ghostBy("/icommands", sid, "slogs", runId).property("result").asString() ;

			return Response.ok(resultValue, ExtMediaType.TEXT_PLAIN_UTF8).build();
		} catch (InterruptedException ex) {
			throw new IOException(ex);
		} catch (ExecutionException ex) {
			throw new IOException(ex);
		}
	}

	@Path("/{sid}/schedule")
	@GET
	@Produces(ExtMediaType.APPLICATION_JSON_UTF8)
	public JsonObject viewScheduleInfo(@PathParam(Def.ICommand.Sid) final String sid) {
		ReadNode sinfo = rsession.ghostBy("/icommands", sid, "schedule");

		JsonObject sinfoJson = new JsonObject().put(Def.Schedule.MINUTE, sinfo.property(Def.Schedule.MINUTE).defaultValue("")).put(Def.Schedule.HOUR, sinfo.property(Def.Schedule.HOUR).defaultValue("")).put(Def.Schedule.DAY, sinfo.property(Def.Schedule.DAY).defaultValue(""))
				.put(Def.Schedule.MONTH, sinfo.property(Def.Schedule.MONTH).defaultValue("")).put(Def.Schedule.WEEK, sinfo.property(Def.Schedule.WEEK).defaultValue("")).put(Def.Schedule.MATCHTIME, sinfo.property(Def.Schedule.MATCHTIME).defaultValue(""))
				.put(Def.Schedule.YEAR, sinfo.property(Def.Schedule.YEAR).defaultValue("")).put(Def.Schedule.ENABLE, sinfo.property(Def.Schedule.ENABLE).defaultValue(false));

		JsonArray slogs = rsession.ghostBy("/icommands", sid, "slogs").children().transform(new Function<Iterator<ReadNode>, JsonArray>() {
			@Override
			public JsonArray apply(Iterator<ReadNode> iter) {
				JsonArray result = new JsonArray();
				while (iter.hasNext()) {
					ReadNode node = iter.next();
					JsonArray userProp = new JsonArray();
					userProp.add(new JsonPrimitive(node.fqn().name()));
					userProp.add(new JsonPrimitive(node.property("runtime").asLong(0)));
					result.add(userProp);
				}

				return result;
			}
		});
		return new JsonObject().put("info", rsession.ghostBy("/menus/script").property("schedule").asString()).put("sinfo", sinfoJson).put("schemaName", JsonParser.fromString("[{'title':'Id'},{'title':'Run Time'}]").getAsJsonArray()).put("slogs", slogs);
	}

	@Path("/{sid}/schedule")
	@POST
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String editScheduleInfo(@PathParam(Def.ICommand.Sid) final String sid, @DefaultValue("0-59") @FormParam(Def.Schedule.MINUTE) final String minute, @DefaultValue("0-23") @FormParam(Def.Schedule.HOUR) final String hour, @DefaultValue("1-31") @FormParam(Def.Schedule.DAY) final String day,
			@DefaultValue("1-12") @FormParam(Def.Schedule.MONTH) final String month, @DefaultValue("1-7") @FormParam(Def.Schedule.WEEK) final String week, @DefaultValue("-1") @FormParam(Def.Schedule.MATCHTIME) final String matchtime,
			@DefaultValue("2014-2020") @FormParam(Def.Schedule.YEAR) final String year, @DefaultValue("false") @FormParam(Def.Schedule.ENABLE) final boolean enable) {

		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode found = wsession.pathBy("/icommands", sid, "schedule");
				found.property(Def.Schedule.MINUTE, minute).property(Def.Schedule.HOUR, hour).property(Def.Schedule.DAY, day).property(Def.Schedule.MONTH, month).property(Def.Schedule.WEEK, week).property(Def.Schedule.MATCHTIME, matchtime).property(Def.Schedule.YEAR, year)
						.property(Def.Schedule.ENABLE, enable);
				return null;
			}
		});

		return sid + (enable ? " rescheduled" : " canceled");
	}

}

class ScriptOutWriter extends Writer {

	private String eventId;
	private EventSourceEntry ese;
	private StringBuilder buffer = new StringBuilder();
	private CountDownLatch latch;

	public ScriptOutWriter(EventSourceEntry ese, String eventId, CountDownLatch latch) throws IOException {
		this.ese = ese;
		this.eventId = eventId;
		this.latch = latch;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		buffer.append(cbuf, off, len);
	}

	public ScriptOutWriter writeLn(String msg) throws IOException {
		super.write(msg);
		this.flush();
		return this;
	}

	@Override
	public void flush() throws IOException {
		try {
			latch.await(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ese.sendTo(eventId, buffer.toString());
		buffer.setLength(0);
	}

	@Override
	public void close() throws IOException {
		flush();
		ese.closeEvent(eventId);
	}

}
