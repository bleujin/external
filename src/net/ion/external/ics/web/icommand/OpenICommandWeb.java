package net.ion.external.ics.web.icommand;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.script.ScriptException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.external.ICSSubCraken;
import net.ion.external.ics.EventSourceEntry;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.web.Webapp;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.StringUtil;
import net.ion.radon.core.ContextParam;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;

@Path("/icommands")
public class OpenICommandWeb implements Webapp{

	private ICommandWeb rweb;
	public OpenICommandWeb(@ContextParam(ICSSubCraken.EntryName) ICSSubCraken icraken, @ContextParam("jsentry") JScriptEngine jengine, @ContextParam("esentry") EventSourceEntry esentry) throws IOException {
		this.rweb = new ICommandWeb(icraken, jengine, esentry) ;
	}

	@Path("/{sid}/run/{runid}")
	@GET @POST
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public Response runICommand(final @PathParam(Def.ICommand.Sid) String sid, @Context HttpRequest request, final @PathParam("runid") String runid) throws IOException, ScriptException {
		ReadSession rsession = rweb.session() ;
		
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
		for (Entry<String, List<String>> entry : request.getUri().getQueryParameters().entrySet()) {
			if (StringUtil.isNotBlank(entry.getKey()))
				params.put(entry.getKey(), entry.getValue());
		}

		for (Entry<String, List<String>> entry : request.getDecodedFormParameters().entrySet()) {
			if (StringUtil.isNotBlank(entry.getKey()))
				params.put(entry.getKey(), entry.getValue());
		}

		final String content = Def.ICommand.ghostBy(rsession, sid).property(Def.ICommand.Content).asString();
		final String method = request.getHttpMethod() ;
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode wnode = wsession.pathBy("/icommands", sid, "run") ;
				wnode.property("scriptid", sid).property("content", content).property("method", method).property("runid", runid).increase("count") ;
				for (Entry<String, List<String>> entry : params.entrySet()) {
					wnode.property("param_" + entry.getKey(), entry.getValue()) ;
				}
				return null;
			}
		}) ;
		

		return Response.ok("", ExtMediaType.TEXT_PLAIN_UTF8).build();
	}
	
	
	@Path("/{sid}/result/{runid}")
	@GET 
	@Produces(ExtMediaType.TEXT_PLAIN_UTF8)
	public String viewResult(final @PathParam(Def.ICommand.Sid) String sid, final @PathParam("runid") String runid){
		ReadSession rsession = rweb.session() ;
		ReadNode find = rsession.ghostBy("/icommands", sid, "slogs", runid) ;
		
		return find.property("result").asString();
	}
	
}
