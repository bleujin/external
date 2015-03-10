package net.ion.external.ics;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.ion.craken.ICSCraken;
import net.ion.external.domain.DomainMaster;
import net.ion.external.domain.DomainSub;
import net.ion.external.ics.common.AppLogSink;
import net.ion.external.ics.common.FavIconHandler;
import net.ion.external.ics.common.HTMLTemplateEngine;
import net.ion.external.ics.common.MyAuthenticationHandler;
import net.ion.external.ics.common.MyEventLog;
import net.ion.external.ics.common.MyStaticFileHandler;
import net.ion.external.ics.common.MyVerifier;
import net.ion.external.ics.common.TraceHandler;
import net.ion.external.ics.openweb.OpenDomainWeb;
import net.ion.external.ics.openweb.OpenScriptWeb;
import net.ion.external.ics.web.DomainEntry;
import net.ion.external.ics.web.DomainWeb;
import net.ion.external.ics.web.ScriptWeb;
import net.ion.external.ics.web.misc.CrakenLet;
import net.ion.external.ics.web.misc.ExportWeb;
import net.ion.external.ics.web.misc.MiscWeb;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.db.manager.script.JScriptEngine;
import net.ion.nradon.EventSourceConnection;
import net.ion.nradon.EventSourceHandler;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.nradon.handler.logging.LoggingHandler;
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
        ICSCraken craken = ICSCraken.create() ;
        final EventSourceEntry esentry = builder.context(EventSourceEntry.EntryName, EventSourceEntry.create());
        DomainEntry dentry = DomainEntry.test(DomainSub.create(craken));
		
		DomainMaster dmaster = DomainMaster.create(new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6"), craken)
					.artImageRoot(new File("./resource/uploadfiles/artimage"))
					.galleryRoot(new File("./resource/uploadfiles/gallery"))
					.afieldFileRoot(new File("./resource/uploadfiles/afieldfile"));
        
        builder.context(ICSCraken.EntryName, craken);
        builder.context(DomainEntry.EntryName, dentry);
        
        final JScriptEngine jsentry = builder.context(JScriptEngine.EntryName, JScriptEngine.create("./resource/loader/lib", Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder.createThreadFactory("script-monitor-thread-%d")), true));
		jsentry.executorService(Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("jscript-thread-%d")));

		ExecutorService nworker = Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("nworker-thread-%d")) ;
        
        final QueryTemplateEngine ve = builder.context(QueryTemplateEngine.EntryName, QueryTemplateEngine.create("my.craken", craken.login()));
        

        final MyEventLog elogger = MyEventLog.create(System.out);
        this.radon = builder.createRadon();
		radon.add(new MyAuthenticationHandler(MyVerifier.test(craken.login())))
			.add("/admin/*", new TraceHandler(craken))
			.add("/favicon.ico", new FavIconHandler())
			.add(new LoggingHandler(new AppLogSink(elogger)))
			.add(new MyStaticFileHandler("./webapps/admin/", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("static-io-thread-%d")), new HTMLTemplateEngine(radon.getConfig().getServiceContext())).welcomeFile("index.html"))
            .add("/admin/*", new PathHandler(DomainWeb.class, ScriptWeb.class, MiscWeb.class, CrakenLet.class, ExportWeb.class).prefixURI("/admin"))
            .add("/open/*", new PathHandler(OpenDomainWeb.class, OpenScriptWeb.class).prefixURI("open"))
            .add("/logging/event/*", new EventSourceHandler() {
					@Override
					public void onOpen(EventSourceConnection econn) throws Exception {
						elogger.onOpen(econn);
					}

					@Override
					public void onClose(EventSourceConnection econn) throws Exception {
						elogger.onClose(econn);
					}
				}).add("/event/{id}", new EventSourceHandler() {
					@Override
					public void onOpen(EventSourceConnection conn) throws Exception {
						esentry.onOpen(conn);
					}

					@Override
					public void onClose(EventSourceConnection conn) throws Exception {
						esentry.onClose(conn);
					}
				})
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
