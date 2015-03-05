package net.ion.bleujin;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import net.coobird.thumbnailator.Thumbnails;
import net.ion.framework.util.IOUtil;

public class TestThumbnail extends TestCase {

	public void testFirst() throws Exception {
		InputStream in = getClass().getResourceAsStream("aaa.jpg") ;
		BufferedImage originalImage = ImageIO.read(in);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Thumbnails.of(originalImage)
		        .size(200, 200)
		        .toOutputStream(out);
		
		ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray()) ;
	}
	
	public void testPipe() throws Exception {
		

	}
	
}
