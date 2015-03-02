package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.net.MediaType;

import net.ion.cms.env.ICSCraken;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.node.ReadNode;

public class GalleryX extends BeanX{

	public GalleryX(ICSCraken rc, ReadNode node) {
		super(rc, node);
	}

	public static GalleryX create(ICSCraken rc, ReadNode node) {
		return new GalleryX(rc, node);
	}
	
	public int galId(){
		return asInt(Gallery.GalId) ;
	}
	
	public String catId(){
		return asString(Gallery.CatId) ;
	}

	public GalleryCategoryX category() throws IOException {
		return rc().findGalleryCategory(catId());
	}

	public InputStream dataStream() throws IOException {
		return asStream("data");
	}

	public String typeCd() {
		return asString(Gallery.TypeCd);
	}

}
