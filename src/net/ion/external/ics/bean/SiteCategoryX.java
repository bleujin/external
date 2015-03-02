package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.List;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.craken.node.crud.ReadChildrenEach;
import net.ion.craken.node.crud.ReadChildrenIterator;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;

import org.apache.lucene.analysis.kr.utils.StringUtil;

public class SiteCategoryX extends AbCategory<SiteCategoryX> {

	private String joiner = "/";
	
	private SiteCategoryX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}

	public static SiteCategoryX create(ICSCraken rc, ReadNode node) {
		return new SiteCategoryX(rc, node);
	}

	public CategoryChildrenX<SiteCategoryX> children() {
		return CategoryChildrenX.siteCategory(this, false);
	}
	
	public CategoryChildrenX<SiteCategoryX> children(boolean ableLeaf) {
		return CategoryChildrenX.siteCategory(this, ableLeaf);
	}
	

	public ArticleChildrenX articles() throws IOException{
		return ArticleChildrenX.create(rc(), catId(), pathBy("/article/" + catId()).childQuery("catid:" + catId())) ;
	}

	public ArticleChildrenX articles(boolean includeBelow) throws IOException {
		if (! includeBelow) articles() ;
		
		StringBuilder ids = pathBy(node().fqn().toString()).walkRefChildren("tree").eachNode(new ReadChildrenEach<StringBuilder>() {
			@Override
			public StringBuilder handle(ReadChildrenIterator iter) {
				StringBuilder result = new StringBuilder(catId());
				while(iter.hasNext()){
					result.append(" " + iter.next().fqn().name()) ;
				}
				return result;
			}
		}) ;
		
		return ArticleChildrenX.create(rc(), catId(), pathBy("/article").childQuery("catid:(" + ids.toString() + ")", true)) ;
	}

	public ArticleX article(int artId) {
		return ArticleX.create(rc(), session().ghostBy("/article/" + catId() + "/"+ artId));
	}

	public TemplateChildrenX templates() throws IOException {
		return TemplateChildrenX.create(this); 
	}

	public TemplateX template(int tplId) {
		return TemplateX.create(rc(), session().ghostBy("/template/" + catId() + "/"+ tplId));
	}


}
