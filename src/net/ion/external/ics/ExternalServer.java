package net.ion.external.ics;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import net.ion.craken.Craken;
import net.ion.external.domain.DomainMaster;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.common.HTMLTemplateEngine;
import net.ion.external.ics.common.MyStaticFileHandler;
import net.ion.external.ics.web.DomainEntry;
import net.ion.external.ics.web.DomainWeb;
import net.ion.external.ics.web.MiscWeb;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.nradon.netty.NettyWebServer;
import net.ion.radon.core.let.PathHandler;

public class ExternalServer {

	private NettyWebServer radon;
	private Status status ;
	
	private enum Status {
		INITED, STARTED, STOPED 
	}
	
	
	public ExternalServer(int port) throws Exception {
		init(port) ;
	}

	private void init(int port) throws Exception {
        RadonConfigurationBuilder builder = RadonConfiguration.newBuilder(port);
        Craken craken = Craken.create() ;
        DomainEntry dentry = DomainEntry.test(DomainSub.create(craken));
		
		DomainMaster dmaster = DomainMaster.create(new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6"), craken)
					.artImageRoot(new File("./resource/uploadfiles/artimage"))
					.galleryRoot(new File("./resource/uploadfiles/gallery"))
					.afieldFileRoot(new File("./resource/uploadfiles/afieldfile"));
        
        builder.context(Craken.EntryName, craken);
        builder.context(DomainEntry.EntryName, dentry);

        this.radon = builder.createRadon();
		radon
            .add("/ics/*", new PathHandler(DomainWeb.class, MiscWeb.class).prefixURI("/ics"))
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

	public static ExternalServer create(int port) throws Exception {
		return new ExternalServer(port);
	}

	
	public ExternalServer start() throws InterruptedException, ExecutionException {
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

	public ExternalServer shutdown() {
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
