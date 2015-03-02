package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.ion.cms.rest.sync.Def;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import com.google.common.collect.Lists;

public class TemplateChildrenX {

	private SiteCategoryX from;
	private List<String> wheres = ListUtil.newList();

	private TemplateChildrenX(SiteCategoryX from) {
		this.from = from ;
	}

	public static TemplateChildrenX create(SiteCategoryX from) {
		return new TemplateChildrenX(from);
	}
	
	public TemplateChildrenX selectList() {
		wheres.add("tplkindcd = 'list'") ;
		return this;
	}

	public TemplateChildrenX selectStory() {
		wheres.add("tplkindcd = 'story'") ;
		return this;
	}

	public XIterable<TemplateX> find() {
		XIterable<TemplateX> result = XIterable.create(from.rc(), from.pathBy("/template/" + from.catId()).children().toList(), TemplateX.class);
		return wheres.size() > 0 ? result.match(StringUtil.join(wheres, " AND ")) : result;
	}

	public TemplateX findById(int tplId) throws IOException {
		return find().findByKey(tplId);
	}

//	public TemplateX findBySeq(int seq) {
//		return Lists.newArrayList(find().ascending("tplid")).get(seq) ;
//	}
	
	public TemplateX listTemplate(int tplId) throws IOException {
		TemplateX result = findById(tplId);
		return result.exists() ? result : ObjectUtil.coalesce(selectList().find().ascending(Def.Template.TplId).iterator().next(), TemplateX.create(from.rc(), from.rc().session().ghostBy("/notfound")));
	}

	public TemplateX listTemplateBySeq(int seq) throws IOException {
		return selectList().find().ascending(Def.Template.TplId).toList().get(seq) ;
	}

	public TemplateX storyTemplate(int tplId) throws IOException {
		TemplateX result = findById(tplId);
		return result.exists() ? result : ObjectUtil.coalesce(selectStory().find().ascending(Def.Template.TplId).iterator().next(), TemplateX.create(from.rc(), from.rc().session().ghostBy("/notfound")));
	}

	public TemplateX storyTemplateBySeq(int seq) throws IOException {
		return selectStory().find().ascending(Def.Template.TplId).toList().get(seq) ;
	}


	
	
}
