package net.ion.external.ics.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.util.HttpHeaderNames;

import net.ion.cms.env.ICSCraken;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.radon.core.ContextParam;
import oracle.jdbc.proxy.annotation.GetProxy;

@Path("/gallery")
public class GalleryWeb implements WebApp{

	private ICSCraken craken;

	public GalleryWeb(@ContextParam("craken") ICSCraken craken) {
		this.craken = craken ;
	}
	
	
	@Path("/")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello(@QueryParam("name") String name){
		return "hello " + name ;
				
	}
	
	@Path("/search/{catId}.json")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput search(@Context HttpResponse response,  @PathParam("catId") final String catId, @QueryParam("limit") final int offset) throws IOException {

		response.getOutputHeaders().putSingle(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_XML);
		Set<String> keys = response.getOutputHeaders().keySet();
		for(String key : keys){
			Debug.line(key);
		}
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				OutputStreamWriter sw = new OutputStreamWriter(output) ;
				OutputHandler ohandler = OutputHandler.createJson(sw) ;
				craken.findGalleryCategory(catId).galleries().offset(offset).find().out(ohandler);
			}
		};
	}
	
	
	
	
}
