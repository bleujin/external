package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;

public class TemplateX extends BeanX{

	private TemplateX(Domain domain, ReadNode node) {
		super(domain, node);
	}
	
	public final static TemplateX create(Domain domain, ReadNode node){
		return new TemplateX(domain, node) ;
	}
	
	public int tplId() {
		return asInt("tplid");
	}
	
	public String catId() {
		return asString(Def.Template.CatId);
	}	

	public boolean exists() {
		return ! node().isGhost();
	}

	public String fileName() {
		return asString(Def.Template.FileName, tplId() + "_index.html") ;
	}

	public String fileName(int artId) throws IOException {
		return category().article(artId).fileName(tplId()) ;
	}

	public SiteCategoryX category() throws IOException {
		return domain().scategory(catId());
	}

	public boolean isList() {
		return "list".equals(asString("kindcd"));
	}


}
