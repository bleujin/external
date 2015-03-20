package net.ion.external.ics.openweb;

import java.io.IOException;

import javax.script.ScriptException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import net.ion.craken.node.ReadSession;
import net.ion.external.ICSSampleCraken;
import net.ion.external.ics.EventSourceEntry;
import net.ion.external.ics.web.Webapp;
import net.ion.external.ics.web.script.ScriptWeb;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.radon.core.ContextParam;

import org.jboss.resteasy.spi.HttpRequest;

@Path("/script")
public class OpenScriptWeb implements Webapp{

	private ScriptWeb refWeb;
	private ICSSampleCraken icraken;
	private ReadSession rsession;
	private JScriptEngine jengine;
	private EventSourceEntry esentry;

	public OpenScriptWeb(@ContextParam(ICSSampleCraken.EntryName) ICSSampleCraken icraken, @ContextParam("jsentry") JScriptEngine jengine, @ContextParam("esentry") EventSourceEntry esentry ) throws IOException{
		this.refWeb = new ScriptWeb(icraken, jengine, esentry) ;
		this.icraken = icraken ;
		this.rsession = icraken.login() ;
		this.jengine = jengine ;
		this.esentry = esentry ;
	}

	@Path("/run/{sid}")
	@GET @POST
	public Response runScript(@PathParam("sid") String sid, @Context HttpRequest request) throws IOException, ScriptException{
		return refWeb.runScript(sid, request) ;
	}
	
	
}


