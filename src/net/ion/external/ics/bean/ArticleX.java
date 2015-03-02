package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.mail.Session;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.cms.rest.sync.Def.Article;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.craken.tree.PropertyId;
import net.ion.craken.tree.PropertyId.PType;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.SetUtil;

import org.apache.lucene.queryparser.classic.ParseException;

public class ArticleX extends BeanX{

	private ArticleX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}

	public static ArticleX create(ICSCraken rc, ReadNode node) {
		return new ArticleX(rc, node);
	}

	public int artId() {
		return asInt(Def.Article.ArtId);
	}

	public String catId() {
		return asString(Def.Article.CatId);
	}
	
	public SiteCategoryX category() throws IOException {
		return rc().findSiteCategory(catId());
	}
	
	public int increasCount(final String propId) throws InterruptedException, ExecutionException{
		return session().tran(new TransactionJob<Integer>() {
			@Override
			public Integer handle(WriteSession wsession) throws Exception {
				return wsession.pathBy(node().fqn()).increase(propId).intValue(1) ;
			}
		}).get() ;
	}
	
	public String toString(){
		return "artId:" + artId() + ", catId:" + catId() ; 
	}

	public XIterable<ArticleX> relateds() {
		final IteratorList<ReadNode> relateds = node().refChildren(Article.Related).iterator() ;
		return XIterable.create(rc(), relateds.toList(), MapUtil.<String, String>newMap(), ArticleX.class) ;
	}

	public Set<String> propKeys() {
		Set<String> result = SetUtil.newSet() ;
		for(PropertyId pid : node().keys()){
			if (pid.type() == PType.REFER) continue ;
			result.add(pid.getString()) ;
		}
		return result ;
	}

	public String fileName(int tplId) {
		return asBoolean(Def.Article.IsUsingUrlLoc) ? asString(Def.Article.ArtFileNm, tplId + "_" + artId() + ".html") : tplId + "_" + artId() + ".html";
	}


}
