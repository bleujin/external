package net.ion.bleujin;

import javaxt.io.Image;
import junit.framework.TestCase;
import net.coobird.thumbnailator.Thumbnails;
import net.ion.framework.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class TestThumbnail extends TestCase {

	public void testFirst() throws Exception {
        File src = new File("./resource/uploadfiles/afieldfile/2015/01/15/aaa.jpg");;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Thumbnails.of(src)
		        .size(200, 200)
		        .toOutputStream(out);
		
		ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray()) ;

        assertEquals(true, input.read() > -1) ;
	}
	
	public void testCrop() throws Exception {
        File src = new File("./resource/uploadfiles/afieldfile/2015/01/15/aaa.jpg");;

        String path = "./test/" + StringUtil.replace(StringUtil.substringBeforeLast(getClass().getName(), "."), ".", "/");
        File cropped = new File(path, "aaa_croped.jpg");

        Image srcImg = new Image(src);
        srcImg.crop(50, 50, 200, 200) ;
        srcImg.saveAs(cropped) ;

        cropped.delete() ;
    }
}
