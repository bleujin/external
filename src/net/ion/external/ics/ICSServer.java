package net.ion.external.ics;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import net.ion.craken.Craken;
import net.ion.external.ics.common.HTMLTemplateEngine;
import net.ion.external.ics.common.MyStaticFileHandler;
import net.ion.external.ics.web.GalleryWeb;
import net.ion.external.ics.web.MiscWeb;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.AbstractResourceHandler;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.nradon.netty.NettyWebServer;
import net.ion.radon.core.let.PathHandler;

public class ICSServer {

	private NettyWebServer radon;
	private Status status ;
	
	private enum Status {
		INITED, STARTED, STOPED 
	}
	
	
	public ICSServer(int port) throws Exception {
		init(port) ;
	}

	private void init(int port) throws Exception {
        RadonConfigurationBuilder builder = RadonConfiguration.newBuilder(port);
        builder.context(Craken.EntryName, Craken.create());

        this.radon = builder.createRadon();
		radon
            .add("/ics/*", new PathHandler(GalleryWeb.class, MiscWeb.class).prefixURI("/ics"))
            .add(new MyStaticFileHandler("./webapps/admin/", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("static-io-thread-%d")), new HTMLTemplateEngine(radon.getConfig().getServiceContext())).welcomeFile("index.html"))
            .add(new HttpHandler() {
                @Override
                public int order() {
                    return 999;
                }

                @Override
                public void onEvent(EventType eventtype, Radon radon) {
                }

                @Override
                public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl hcontrol) throws Exception {
                    response.status(404).content("404 page").end();
                }
            });
		
		
		this.status = Status.INITED ;
	}

	public static ICSServer create(int port) throws Exception {
		return new ICSServer(port);
	}

	
	public ICSServer start() throws InterruptedException, ExecutionException {
		if (status == Status.STARTED) return this ;
		
		radon.start().get();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				shutdown();
			}
		});

		
		this.status = Status.STARTED ;
		return this;
	}

	public ICSServer shutdown() {
		if (status != Status.STARTED) return this ;
		
		try {
			radon.stop().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		this.status = Status.STOPED ;
		return this ;
	}

	
	
}
