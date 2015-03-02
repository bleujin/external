package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;

public class TemplateX extends BeanX{

	private TemplateX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}
	
	public final static TemplateX create(ICSCraken rc, ReadNode node){
		return new TemplateX(rc, node) ;
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
		return asString(Def.Template.ListFileNm, tplId() + "_index.html") ;
	}

	public String fileName(int artId) throws IOException {
		return category().article(artId).fileName(tplId()) ;
	}

	public SiteCategoryX category() throws IOException {
		return rc().findSiteCategory(catId());
	}


}
