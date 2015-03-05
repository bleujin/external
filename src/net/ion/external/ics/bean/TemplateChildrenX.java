package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.List;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.ChildQueryRequest;
import net.ion.external.domain.Domain;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

public class TemplateChildrenX {

	private List<String> wheres = ListUtil.newList();
	private Domain domain;
	private ReadSession session;
	private ChildQueryRequest request;

	private TemplateChildrenX(Domain domain, ReadSession session, ChildQueryRequest request) {
		this.domain = domain ;
		this.session = session ;
		this.request = request ;
	}

	public static TemplateChildrenX create(Domain domain, ChildQueryRequest request) throws IOException {
		return new TemplateChildrenX(domain, domain.session(), request);
	}
	
	public TemplateChildrenX selectList() {
		wheres.add("kindcd = 'list'") ;
		return this;
	}

	public TemplateChildrenX selectStory() {
		wheres.add("kindcd = 'story'") ;
		return this;
	}

	public XIterable<TemplateX> find() throws IOException {
		XIterable<TemplateX> result = XIterable.create(domain, request.find().toList(), MapUtil.<String,String>newMap(), TemplateX.class);
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
		return result.exists() ? result : ObjectUtil.coalesce(selectList().find().ascending(Def.Template.TplId).iterator().next(), TemplateX.create(domain, session.ghostBy("/notfound")));
	}

	public TemplateX listTemplateBySeq(int seq) throws IOException {
		return selectList().find().ascending(Def.Template.TplId).toList().get(seq) ;
	}

	public TemplateX storyTemplate(int tplId) throws IOException {
		TemplateX result = findById(tplId);
		return result.exists() ? result : ObjectUtil.coalesce(selectStory().find().ascending(Def.Template.TplId).iterator().next(), TemplateX.create(domain, session.ghostBy("/notfound")));
	}

	public TemplateX storyTemplateBySeq(int seq) throws IOException {
		return selectStory().find().ascending(Def.Template.TplId).toList().get(seq) ;
	}


	
	
}
