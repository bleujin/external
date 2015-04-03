package net.ion.bleujin;

import junit.framework.TestCase;
import net.ion.icrawler.Site;
import net.ion.icrawler.Spider;
import net.ion.icrawler.pipeline.DebugPipeline;
import net.ion.icrawler.processor.SimplePageProcessor;
import net.ion.icrawler.scheduler.MaxLimitScheduler;
import net.ion.icrawler.scheduler.QueueScheduler;

public class TestCrawl extends TestCase{
	public void testSimple() throws Exception {
		Site site = Site.create("http://bleujin.tistory.com").sleepTime(50);
		
		SimplePageProcessor processor = new SimplePageProcessor("http://bleujin.tistory.com/*");
		Spider spider = site.newSpider(processor).scheduler(new MaxLimitScheduler(new QueueScheduler(), 10));

		spider.addPipeline(new DebugPipeline()).run();
	}
}
