package net.ion.external.ics.bean;

import java.io.StringWriter;
import java.io.Writer;

import net.ion.external.ics.bean.OutputHandler;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.framework.util.Debug;

import org.apache.ecs.xml.XML;

public class TestOutputHandler extends TestPOJOBase{

	
	public void testXiterableToJson() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		OutputHandler ohandler = OutputHandler.createJson(new StringWriter()) ;
		category.articles(true).where("artid >= 7000").skip(0).offset(10).sort("artid=asc").find().out(ohandler, "name", "age", "catid").debugPrint() ;
	}

	
	public void testBeanXToJson() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		OutputHandler ohandler = OutputHandler.createJson(new StringWriter()) ;
		category.out(ohandler).debugPrint() ;
	}
	
	
	public void testBeanXToXml() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;

		OutputHandler ohandler = OutputHandler.createXml(new StringWriter()) ;
		category.out(ohandler).debugPrint();
	}
	
	public void testXIterableXML() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		OutputHandler ohandler = OutputHandler.createXml(new StringWriter()) ;
		category.articles(true).where("artid >= 7000").skip(0).offset(10).sort("artid=asc").find().out(ohandler, "name", "age", "catid").debugPrint() ;		
	}


	public void testXIterableHTML() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		OutputHandler ohandler = OutputHandler.createHtml(new StringWriter()) ;
		category.articles(true).where("artid >= 7000").skip(0).offset(10).sort("artid=asc").find().out(ohandler, "name", "age", "catid").debugPrint() ;		
	}
	
}
