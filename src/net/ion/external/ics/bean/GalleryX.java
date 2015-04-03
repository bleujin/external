package net.ion.external.ics.bean;

import com.google.common.base.Preconditions;

import javaxt.io.Image;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.domain.Domain;
import net.ion.framework.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public InputStream resizeWith(final int width, final int height) throws IOException {
        final String propId = String.format("resize%s_%s", width, height) ;

        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
//                int originalWidth = image.getWidth() ;
//                int originalHeight = image.getHeight() ;
//                int validwidth = Math.min(width, originalWidth) ;
//                int validheight = Math.min(height, originalHeight) ;
                
                image.resize(width, height) ;
                return new ByteArrayInputStream(image.getByteArray()) ;
            }
        });
	}

    public InputStream cropWith(final int x, final int y, final int width, final int height) throws IOException {
    	final String propId = "crop"+x+"_"+y+"_"+width+"_"+height ;
    	
        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
                int originalWidth = image.getWidth() ;
                int originalHeight = image.getHeight() ;

                int validx = Math.max(x, 0) ;
                int validy = Math.max(y, 0) ;
                int validwidth = Math.min(width, originalWidth - validx) ;
                int validHeight = Math.min(height, originalHeight - validy) ;
                
                image.crop(validx , validy, validwidth, validHeight) ;
                return new ByteArrayInputStream(image.getByteArray()) ;
            }
        });
    }

    private InputStream handleImage(final String propId, ImageHandler handler) throws IOException {
        if(hasProperty(propId)) {
            return asStream(propId) ;
        }

        if(dataStream() == BeanX.BLANKSTREAM) {
            return BeanX.BLANKSTREAM ;
        }

        Image image = new Image(dataStream());

        final InputStream in = handler.handle(image) ;
        session().tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                wsession.pathBy(node().fqn()).blob(propId, in) ;
                return null;
            }
        });
        
        IOUtil.close(in);

        return asStream(propId) ;
    }
}

interface ImageHandler {
    InputStream handle(Image image) ;
}