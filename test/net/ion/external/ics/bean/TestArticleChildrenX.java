package net.ion.external.ics.bean;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.XIterable;
import net.ion.framework.parse.gson.Gson;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

import org.apache.lucene.search.BooleanQuery;

import com.google.common.base.Function;

public class TestArticleChildrenX extends TestPOJOBase{

	public void testArticleChildren() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("cxm") ;
		
		category.articles().where("artid >= 7000").skip(0).offset(10).sort("artid").debugPrint() ;
	}
	
	public void testBelowYes() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		category.articles(true).where("artid >= 7000").skip(0).offset(10).sort("artid").debugPrint() ;
	}

	
	
	public void testMatch() throws Exception {
		SiteCategoryX category = rc.findSiteCategory("dev") ;
		
		XIterable<ArticleX> found = category.articles(true).where("artid >= 7000").skip(0).offset(10).sort("artid").find() ;
		// age=20
		found.match("age>=20 and age <= 21").debugPrint(); 
	}
	
	

	
	
	
	public void xtestWalkChildren() throws Exception {
		session.pathBy("/article/cxm").children().where("not(age = 21)").debugPrint(); 
		session.pathBy("/article/cxm").childQuery("").where("not(age = 21)").find().debugPrint();
		
//		session.root().walkChildren().where("age=20").debugPrint();
//		session.pathBy("/article/cxm").children().where("age=20").debugPrint();
		session.pathBy("/article").walkChildren().where("age=20").debugPrint();

//		session.pathBy("/article/cxm").childQuery("", true).where("name=bleujin").find().debugPrint();
		
		StringBuilder sb = new StringBuilder("cxm") ;
		for (int i = 0; i < 100000; i++) {
			sb.append(" dev" + i ) ;
		}
		BooleanQuery.setMaxClauseCount(120000);
		
		
		session.pathBy("/article/cxm").childQuery("catid:(" + sb.toString() + ")", true).where("age > 20").find().debugPrint();
	}
	
}
