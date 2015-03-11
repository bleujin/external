package net.ion.external.ics.bean;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ReadChildrenEach;
import net.ion.craken.node.crud.ReadChildrenIterator;
import net.ion.external.domain.Domain;

public class SiteCategoryX extends AbCategory<SiteCategoryX> {

	private String joiner = "/";
	
	private SiteCategoryX(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public static SiteCategoryX create(Domain domain, ReadNode node) {
		return new SiteCategoryX(domain, node);
	}

	public CategoryChildrenX<SiteCategoryX> chidren() throws IOException {
		return CategoryChildrenX.siteCategory(domain(), session().pathBy("/datas/scat").childQuery(new TermQuery(new Term("parent", catId()))));
	}

	public ArticleChildrenX articles() throws IOException{
		return ArticleChildrenX.create(domain(), pathBy("/datas/article/" + catId()).childQuery("catid:" + catId())) ;
	}

	public ArticleX article(int artId) {
		return ArticleX.create(domain(), session().ghostBy("/datas/article/" + catId() + "/"+ artId));
	}

	public TemplateChildrenX templates() throws IOException {
		return TemplateChildrenX.create(domain(), pathBy("/datas/template/" + catId()).childQuery("catid:" + catId())); 
	}

	public TemplateX template(int tplId) {
		return TemplateX.create(domain(), session().ghostBy("/datas/template/" + catId() + "/"+ tplId));
	}

	public XIterable<AfieldMetaX> afieldMetas() {
		return XIterable.<AfieldMetaX>create(domain(), node().refs("afields").toList(), AfieldMetaX.class) ;
		
	}


	
}
