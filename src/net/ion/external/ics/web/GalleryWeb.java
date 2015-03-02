package net.ion.external.ics.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import net.ion.cms.env.ICSCraken;
import net.ion.external.ics.bean.OutputHandler;
import net.ion.radon.core.ContextParam;

import org.jboss.resteasy.spi.HttpResponse;

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
