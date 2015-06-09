package net.ion.external.ics.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

import javaxt.io.Image;

import net.ion.cms.rest.sync.Def.Gallery;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.external.domain.Domain;
import net.ion.framework.util.IOUtil;

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
        final String propId = String.format("resize%s_%s", width, height);
        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
                image.resize(width, height);
                return new ByteArrayInputStream(image.getByteArray());
            }
        });
    }

    public void resizeAs(final int width, final int height) throws IOException {
        final Image stream = new Image(dataStream()) ;

        stream.resize(width, height);

        updateDataStream(stream, width, height);

        resizeWith(137, 137) ;          // refresh thumbnail image
    }

    public InputStream cropWith(final int x, final int y, final int width, final int height) throws IOException {
        final String propId = "crop" + x + "_" + y + "_" + width + "_" + height;

        return handleImage(propId, new ImageHandler() {
            @Override
            public InputStream handle(Image image) {
                crop(image, x, y, width, height) ;
                return new ByteArrayInputStream(image.getByteArray());
            }
        });
    }

    public void cropAs(final int x, final int y, final int width, final int height) throws IOException {
        final Image original = new Image(dataStream()) ;

        crop(original, x, y, width, height) ;

        updateDataStream(original, width, height);
    }

    private void crop(Image image, int x, int y, int width, int height) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        int validx = Math.max(x, 0);
        int validy = Math.max(y, 0);
        int validwidth = Math.min(width, originalWidth - validx);
        int validHeight = Math.min(height, originalHeight - validy);

        image.crop(validx, validy, validwidth, validHeight);
    }

    private void updateDataStream(final Image image, final int width, final int height) {
        session().tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                WriteNode wnode = wsession.pathBy(node().fqn());

                clearOldData(wnode);
                updateData(wnode, image);
                updateThumbnail(wnode, image);

                return null;
            }

            private void clearOldData(WriteNode node) {
                Map<String, Object> props = node.toReadNode().toPropertyMap(0);
                Pattern p = Pattern.compile("^(resize|crop)[0-9]+\\_[0-9]+$") ;

                for(String propId : props.keySet()) {
                    if(p.matcher(propId).matches() && node.property(propId).type().equals(PropertyValue.VType.BLOB)) {
                        node.unset(propId) ;
                    }
                }
            }

            private void updateData(WriteNode node, Image image) {
                node.property("width", width).property("height", height).blob("data", new ByteArrayInputStream(image.getByteArray()));
            }

            private void updateThumbnail(WriteNode node, Image image) {
                image.resize(137, 137);
                node.blob("resize137_137", new ByteArrayInputStream(image.getByteArray())) ;
            }
        }) ;
    }

    private InputStream handleImage(final String propId, ImageHandler handler) throws IOException {
        if (hasProperty(propId)) {
            return asStream(propId);
        }

        if (dataStream() == BeanX.BLANKSTREAM) {
            return BeanX.BLANKSTREAM;
        }

        Image image = new Image(dataStream());

        final InputStream in = handler.handle(image);
        session().tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                wsession.pathBy(node().fqn()).blob(propId, in);
                return null;
            }
        });

        IOUtil.close(in);

        return asStream(propId);
    }
}

interface ImageHandler {
    InputStream handle(Image image);
}