package net.ion.bleujin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.store.WorkspaceConfigBuilder;
import net.ion.framework.util.Debug;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.IOUtil;
import net.ion.icrawler.Site;
import net.ion.icrawler.Spider;
import net.ion.icrawler.pipeline.DebugPipeline;
import net.ion.icrawler.processor.SimplePageProcessor;
import net.ion.icrawler.scheduler.MaxLimitScheduler;
import net.ion.icrawler.scheduler.QueueScheduler;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.config.CentralConfig;
import net.ion.nsearcher.index.IndexJob;
import net.ion.nsearcher.index.IndexSession;
import net.ion.nsearcher.index.Indexer;
import net.ion.nsearcher.search.Searcher;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.lucene.store.Directory;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ClusteringConfigurationBuilder;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.EvictionConfigurationBuilder;
import org.infinispan.lucene.directory.BuildContext;
import org.infinispan.lucene.directory.DirectoryBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class TestIndexMany extends TestCase {
	
	public void testCrawl() throws Exception {
		Site site = Site.create("http://bleujin.tistory.com").sleepTime(50);

		SimplePageProcessor processor = new SimplePageProcessor("http://bleujin.tistory.com/*");
		Spider spider = site.newSpider(processor).scheduler(new MaxLimitScheduler(new QueueScheduler(), 10));

		spider.addPipeline(new DebugPipeline()).run();
	}

	public void testSaveMany() throws Exception {

		File homeDir = new File("C:/crawl/enha/wiki/멕시코");
		IOFileFilter filter = new IOFileFilter() {
			public boolean accept(File file, String s) {
				return true;
			}

			public boolean accept(File file) {
				return true;
			}
		};

		Collection<File> files = FileUtil.listFiles(homeDir, filter, filter);
		Debug.line(files.size());
	}

	public void testFileList() throws Exception {
		Craken r = Craken.create();
		r.createWorkspace("enha", WorkspaceConfigBuilder.indexDir("./resource/enha"));

		ReadSession session = r.login("enha");

		session.tran(new TransactionJob<Void>() {
			
			private AtomicInteger count = new AtomicInteger(0) ;
			private long start = System.currentTimeMillis() ;
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				File homeDir = new File("C:/crawl/enha/wiki");
				saveProperty(wsession, homeDir, "");

				return null;
			}
			
			
			private void saveProperty(WriteSession wsession, File file, String path) throws IOException{
				if (file.isDirectory()) {
					for (File sfile : file.listFiles()) {
						saveProperty(wsession, sfile, path + "/" + sfile.getName());
					}
				} else {
					String content = IOUtil.toStringWithClose(new FileInputStream(file), "UTF-8");
					wsession.pathBy(path).property("content", content);
					
					int icount = count.incrementAndGet();
					if ((icount % 500) == 0){
						System.out.println(count.get() + " committed. elapsed time for unit : " + (System.currentTimeMillis() - start));
						this.start = System.currentTimeMillis() ;
						wsession.continueUnit(); 
					}
				}
			}
		});

		// session.pathBy(parentName).children().debugPrint();
	}
	
	
	

	public void testIndexManyAtSearchStore() throws Exception {
		String path = "./resource/isearch" ;
		ClusteringConfigurationBuilder meta_configBuilder = new ConfigurationBuilder().persistence().passivation(false)
				.addSingleFileStore().fetchPersistentState(false).preload(true).shared(false).purgeOnStartup(false).ignoreModifications(false).location(path)
				.async().disable().flushLockTimeout(300000).shutdownTimeout(2000)
				.modificationQueueSize(100).threadPoolSize(10).clustering() ;

		EvictionConfigurationBuilder chunk_configBuilder = new ConfigurationBuilder().persistence().passivation(false)
				.addSingleFileStore().fetchPersistentState(false).preload(true).shared(false).purgeOnStartup(false).ignoreModifications(false).location(path)
				.async().disable().flushLockTimeout(300000).shutdownTimeout(2000).modificationQueueSize(100).threadPoolSize(10).clustering()
				.eviction().maxEntries(30) ;
		
		DefaultCacheManager dcm = new DefaultCacheManager("./resource/config/craken-cache-config.xml") ;
		dcm.defineConfiguration("is-meta", meta_configBuilder.build()) ;
		dcm.defineConfiguration("ics_chunk", chunk_configBuilder.build()) ;
		
		dcm.start(); 
		
		Cache<?, ?> metaCache = dcm.getCache("is-meta");
		Cache<?, ?> chunkCache = dcm.getCache("ics_chunk");

		BuildContext bcontext = DirectoryBuilder.newDirectoryInstance(metaCache, chunkCache, metaCache, "isindex");
		bcontext.chunkSize(1024 * 1024);
		Directory directory = bcontext.create();
		Central central = CentralConfig.oldFromDir(directory).build();
	
		Indexer indexer = central.newIndexer() ;
	
		indexer.index(new IndexJob<Void>() {
			AtomicInteger count = new AtomicInteger(0) ;
			long start = System.currentTimeMillis() ;
			@Override
			public Void handle(IndexSession isession) throws Exception {
				File homeDir = new File("C:/crawl/enha/wiki");
				saveProperty(isession, homeDir, "");
				return null;
			}
			
			private void saveProperty(IndexSession isession, File file, String path) throws IOException{
				if (file.isDirectory()) {
					for (File sfile : file.listFiles()) {
						saveProperty(isession, sfile, path + "/" + sfile.getName());
					}
				} else {
					String content = IOUtil.toStringWithClose(new FileInputStream(file), "UTF-8");
					isession.newDocument(path).text("content", content).update();
					
					int icount = count.incrementAndGet();
					if ((icount % 500) == 0){
						System.out.println(count.get() + " committed. elapsed time for unit : " + (System.currentTimeMillis() - start));
						this.start = System.currentTimeMillis() ;
						isession.continueUnit(); 
					}
				}
			}
		}) ;
		
		dcm.stop(); 
	}

	
	public void testSearch() throws Exception {
		String path = "./resource/isearch" ;
		ClusteringConfigurationBuilder meta_configBuilder = new ConfigurationBuilder().persistence().passivation(false)
				.addSingleFileStore().fetchPersistentState(false).preload(true).shared(false).purgeOnStartup(false).ignoreModifications(false).location(path)
				.async().disable().flushLockTimeout(300000).shutdownTimeout(2000)
				.modificationQueueSize(100).threadPoolSize(10).clustering() ;

		EvictionConfigurationBuilder chunk_configBuilder = new ConfigurationBuilder().persistence().passivation(false)
				.addSingleFileStore().fetchPersistentState(false).preload(true).shared(false).purgeOnStartup(false).ignoreModifications(false).location(path)
				.async().disable().flushLockTimeout(300000).shutdownTimeout(2000).modificationQueueSize(100).threadPoolSize(10).clustering()
				.eviction().maxEntries(30) ;
		
		DefaultCacheManager dcm = new DefaultCacheManager("./resource/config/craken-cache-config.xml") ;
		dcm.defineConfiguration("is-meta", meta_configBuilder.build()) ;
		dcm.defineConfiguration("ics_chunk", chunk_configBuilder.build()) ;
		
		dcm.start(); 
		
		Cache<?, ?> metaCache = dcm.getCache("is-meta");
		Cache<?, ?> chunkCache = dcm.getCache("ics_chunk");

		BuildContext bcontext = DirectoryBuilder.newDirectoryInstance(metaCache, chunkCache, metaCache, "isindex");
		bcontext.chunkSize(1024 * 1024);
		Directory directory = bcontext.create();
		Central central = CentralConfig.oldFromDir(directory).build();

		
		Searcher searcher = central.newSearcher() ;
		Debug.line(searcher.createRequest("육군").find().totalCount()) ;
		
		dcm.stop(); 
	}

}
