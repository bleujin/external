package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.kr.utils.StringUtil;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Filter;

import net.ion.cms.env.ICSCraken;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.NodeCommon;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.convert.Predicates;
import net.ion.craken.node.crud.ChildQueryRequest;
import net.ion.craken.node.crud.ChildQueryResponse;
import net.ion.craken.node.crud.Filters;
import net.ion.craken.node.crud.PredicatedResponse;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.craken.node.crud.util.ResponsePredicate;
import net.ion.craken.node.crud.util.ResponsePredicates;
import net.ion.craken.tree.Fqn;
import net.ion.framework.db.Page;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.nsearcher.search.filter.FilterUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class ArticleChildrenX {

	private ICSCraken rc;
	private ChildQueryRequest queryRequest;
	private Map<String, String> param = MapUtil.newMap() ;
	private final String catId;

	private ArticleChildrenX(ICSCraken rc, String catId, ChildQueryRequest queryRequest) {
		this.rc = rc ;
		this.queryRequest = queryRequest;
		this.catId = catId ;
		param.put("catId", catId) ;
	}

	public static ArticleChildrenX create(ICSCraken rc, String catId, ChildQueryRequest queryRequest) {
		return new ArticleChildrenX(rc, catId, queryRequest);
	}

	public ArticleChildrenX where(String expression) {
		queryRequest.where(expression);
		putParam("search", expression) ;
		return this;
	}

	public ArticleChildrenX skip(int skip) {
		queryRequest.skip(skip);
		putParam("skip", skip);
		return this;
	}

	public ArticleChildrenX offset(int offset) {
		queryRequest.offset(offset);
		putParam("offset", offset);
		return this;
	}

	public ArticleChildrenX page(Page page) {
		queryRequest.skip(page.getStartLoc()).offset(page.getEndLoc());
		return this ;
	}

	public ArticleChildrenX sort(String sortExpr) {
		String[] exprs = StringUtil.split(sortExpr, "&&");
		for (String expr : exprs) {
			String[] term = StringUtil.split(expr, "= ");

			if (term.length == 1)
				queryRequest.ascending(term[0]);
			else if (term.length == 2) {
				queryRequest = ("desc".equalsIgnoreCase(term[1])) ? queryRequest.descending(term[0]) : queryRequest.ascending(term[0]);
			}
		}
		putParam("sort", sortExpr);
		return this;
	}

	public void debugPrint() throws IOException {
		find().debugPrint(); 
	}

	public SiteCategoryX from() throws IOException{
		return rc.findSiteCategory(catId) ;
	}
	
	public String catId(){
		return catId ;
	}

	
	public XIterable<ArticleX> find() throws IOException {
		return XIterable.create(rc, requestFind().toList(), param, ArticleX.class);
	}

	private ChildQueryResponse requestFind() throws IOException{
		return queryRequest.find() ;
	}
	
	private void putParam(String name, Object value) {
		param.put(name, ObjectUtil.toString(value)) ;
	}

}

