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

	public InputStream dataStreamWithSize(final int width, final int height) throws IOException {
        final String propId = String.format("data%sx%s", width, height) ;

        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
                int originalWidth = image.getWidth() ;
                int originalHeight = image.getHeight() ;

                Preconditions.checkArgument(width <= originalWidth, "width too large : " + width);
                Preconditions.checkArgument(height <= originalHeight, "height too large : " + height);

                image.resize(width, height) ;
                return new ByteArrayInputStream(image.getByteArray()) ;
            }
        });
	}

    public InputStream dataStreamWithCrop(String propId, final int x, final int y, final int width, final int height) throws IOException {
        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
                int originalWidth = image.getWidth() ;
                int originalHeight = image.getHeight() ;

                Preconditions.checkArgument(x >= 0 && x + width <= originalWidth, "Invalid x position");
                Preconditions.checkArgument(y >= 0 && y + height <= originalHeight, "Invalid y position");

                image.crop(x, y, width, height) ;
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

        return asStream(propId) ;
    }
}

interface ImageHandler {
    InputStream handle(Image image) ;
}