package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;

public class GalleryCategoryX extends AbCategory<GalleryCategoryX> {

	private GalleryCategoryX(Domain domain, ReadNode node) {
		super(domain, node);
	}
	
	public final static GalleryCategoryX create(Domain domain, ReadNode node){
		return new GalleryCategoryX(domain, node) ;
	}
	
	public GalleryChildrenX galleries() throws IOException{
		return GalleryChildrenX.create(domain(), pathBy("/datas/gallery/" + catId()).childQuery("catid:" + catId())) ;
	}
	
//	public CategoryChildrenX<GalleryCategoryX> children(boolean ableLeaf) {
//		return CategoryChildrenX.galleryCategory(this, ableLeaf);
//	}

	
	public GalleryX gallery(int galId) {
		return GalleryX.create(domain(), session().ghostBy("/datas/gallery/" + catId() + "/" + galId));
	}
	

}
