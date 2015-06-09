package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ChildQueryRequest;
import net.ion.craken.node.crud.ChildQueryResponse;
import net.ion.external.domain.Domain;
import net.ion.framework.db.Page;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import org.apache.lucene.queryparser.classic.ParseException;

import com.google.common.base.Function;

public class GalleryChildrenX {


	private Domain domain;
	private ChildQueryRequest queryRequest;
	private Map<String, String> param = MapUtil.newMap() ;

	private GalleryChildrenX(Domain domain, ChildQueryRequest queryRequest) {
		this.domain = domain ;
		this.queryRequest = queryRequest;
		param.put("domain", domain.getId()) ;
	}

	public static GalleryChildrenX create(Domain domain, ChildQueryRequest queryRequest) {
		return new GalleryChildrenX(domain, queryRequest);
	}

	public <T> T each(Function<Iterator<ArticleX>, T> fn) throws IOException {
		final IteratorList<ReadNode> iter = queryRequest.find().iterator();
		return fn.apply(new Iterator<ArticleX>() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ArticleX next() {
				return ArticleX.create(domain, iter.next());
			}

			@Override
			public void remove() {
			}
		});
	}

	public GalleryChildrenX where(String expression) {
		queryRequest.where(expression);
		putParam("search", expression) ;
		return this;
	}

	public GalleryChildrenX skip(int skip) {
		queryRequest.skip(skip);
		putParam("skip", skip);
		return this;
	}

	public GalleryChildrenX offset(int offset) {
		queryRequest.offset(offset);
		putParam("offset", offset);
		return this;
	}

	public GalleryChildrenX page(Page page) {
		queryRequest.skip(page.getStartLoc()).offset(page.getEndLoc());
		return this ;
	}

	public GalleryChildrenX sort(String sortExpr) {
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

    public GalleryChildrenX descendingNum(String field) {
        queryRequest.descendingNum(field) ;
        return this ;
    }

    public GalleryChildrenX ascendingNum(String field) {
        queryRequest.ascendingNum(field) ;
        return this ;
    }

	public void debugPrint() throws IOException {
		each(new Function<Iterator<ArticleX>, Void>() {
			@Override
			public Void apply(Iterator<ArticleX> iter) {
				while (iter.hasNext()) {
					Debug.line(iter.next());
				}
				return null;
			}
		});
	}


	public XIterable<GalleryX> find() throws IOException {
		return XIterable.create(domain, requestFind().toList(), param, GalleryX.class);
	}

	private ChildQueryResponse requestFind() throws IOException{
		return queryRequest.find() ;
	}
	
	private void putParam(String name, Object value) {
		param.put(name, ObjectUtil.toString(value)) ;
	}

	public GalleryChildrenX query(String query) throws ParseException {
		if (StringUtil.isNotBlank(query)) queryRequest.query(query) ;
		return this;
	}
}
