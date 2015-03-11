package net.ion.external.ics.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.coobird.thumbnailator.Thumbnails;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.domain.Domain;

public class GalleryX extends BeanX {

	public GalleryX(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public static GalleryX create(Domain domain, ReadNode node) {
		return new GalleryX(domain, node);
	}

	public int galId() {
		return asInt(Gallery.GalId);
	}

	public String catId() {
		return asString(Gallery.CatId);
	}

	public GalleryCategoryX category() throws IOException {
		return domain().datas().gcategory(catId());
	}

	public InputStream dataStream() throws IOException {
		return asStream("data");
	}

	public String typeCd() {
		return asString(Gallery.TypeCd);
	}

	public InputStream dataStreamWithSize(int width, int height) throws IOException {
		
		final String resizedPropName = String.format("data%sx%s", width, height) ;
		
		if(hasProperty(resizedPropName)) {
			return asStream(resizedPropName) ;
		} else {
			InputStream dataStream = dataStream();
			if (dataStream == BeanX.BLANKSTREAM) return BeanX.BLANKSTREAM ; 
					
			ByteArrayOutputStream out = new ByteArrayOutputStream() ;
			Thumbnails.of(dataStream).size(width, height).toOutputStream(out);
		
			final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
			session().tran(new TransactionJob<Void>() {
				@Override
				public Void handle(WriteSession wsession) throws Exception {
					wsession.pathBy(node().fqn()).blob(resizedPropName, in) ;
					return null;
				}
			});
			
			return asStream(resizedPropName) ;
		}
	}
}
