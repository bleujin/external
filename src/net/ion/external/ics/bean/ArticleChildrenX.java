package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ChildQueryRequest;
import net.ion.craken.node.crud.ChildQueryResponse;
import net.ion.external.domain.Domain;
import net.ion.framework.db.Page;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;

import org.apache.lucene.analysis.kr.utils.StringUtil;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.QueryWrapperFilter;

public class ArticleChildrenX {

	private ChildQueryRequest queryRequest;
	private Map<String, String> param = MapUtil.newMap() ;
	private Domain domain;

	private ArticleChildrenX(Domain domain, ChildQueryRequest queryRequest) {
		this.queryRequest = queryRequest;
		this.domain = domain ;
		param.put("domainId", domain.getId()) ;
	}

	public static ArticleChildrenX create(Domain domain, ChildQueryRequest queryRequest) {
		return new ArticleChildrenX(domain, queryRequest);
	}

	public ArticleChildrenX where(String expression) {
		queryRequest.where(expression);
		putParam("search", expression) ;
		return this;
	}

	public ArticleChildrenX query(String query) throws ParseException{
		if (StringUtil.isNotBlank(query)) queryRequest.query(query) ;
		
		return this ;
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

	public String domainId(){
		return domain.getId() ;
	}
	
	public XIterable<ArticleX> find() throws IOException {
		long start = System.currentTimeMillis() ;
		List<ReadNode> list = requestFind().toList();
		putParam("elapsedTime", System.currentTimeMillis() - start);
		return XIterable.create(domain, list, param, ArticleX.class);
	}

	private ChildQueryResponse requestFind() throws IOException{
		return queryRequest.find() ;
	}
	
	private void putParam(String name, Object value) {
		param.put(name, ObjectUtil.toString(value)) ;
	}

}

