package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.kr.utils.StringUtil;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.ReadChildrenEach;
import net.ion.craken.node.crud.ReadChildrenIterator;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.ListUtil;

public class GalleryCategoryX extends AbCategory<GalleryCategoryX> {

	private GalleryCategoryX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}
	
	public final static GalleryCategoryX create(ICSCraken rc, ReadNode node){
		return new GalleryCategoryX(rc, node) ;
	}
	
	public GalleryChildrenX galleries() throws IOException{
		return GalleryChildrenX.create(rc(), catId(), pathBy("/gallery/" + catId()).childQuery("catid:" + catId())) ;
	}

	public GalleryChildrenX galleries(boolean includeBelow) throws IOException {
		if (! includeBelow) galleries() ;
		
		StringBuilder ids = pathBy(node().fqn().toString()).walkChildren().eachNode(new ReadChildrenEach<StringBuilder>() {
			@Override
			public StringBuilder handle(ReadChildrenIterator iter) {
				StringBuilder result = new StringBuilder(catId());
				while(iter.hasNext()){
					result.append(" " + iter.next().fqn().name()) ;
				}
				return result;
			}
		}) ;
		
		return GalleryChildrenX.create(rc(), catId(), pathBy("/gallery").childQuery("catid:(" + ids.toString() + ")", true)) ;
	}
	
	public CategoryChildrenX<GalleryCategoryX> children(boolean ableLeaf) {
		return CategoryChildrenX.galleryCategory(this, ableLeaf);
	}

	
	public GalleryX gallery(int galId) {
		return GalleryX.create(rc(), session().ghostBy("/gallery/" + catId() + "/" + galId));
	}
	

}
