package net.ion.external.ics.setup;

import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.FileUtil;
import net.ion.framework.util.IOUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.lucene.analysis.kr.utils.StringUtil;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestGallerySetup extends TestSetup {

	
	public void testGalleryCategory() throws Exception {
		String procSQL ="select '/gcat/' || galCatId fqn, galCatId catId, galUpperCatId parent, galCatNm name" +
						" from gallery_category_tblc";
		
		Debug.line(saveToCraken(procSQL) + " applied");
	}
	
	public void testGalleryCategoryReference() throws Exception {
		String procSQL ="select '/gcat/' || galUpperCatId fqn, '/gcat/' ||galCatId child from gallery_category_tblc " +
						" where galUpperCatId != 'root' connect by galUpperCatId = prior galCatId start with galupperCatId = 'root'" ;

		Integer result = dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				final AtomicInteger count = new AtomicInteger() ;
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).refTos("tree", rs.getString("child")) ;
							count.incrementAndGet() ;
						}
						return null;
					}
				}) ;
				return count.get();
			}
		}) ;
		
		Debug.line(result + " applied");
	}
	

	public void testGallery() throws Exception {
		
		super.resetChildren("/gallery");
		
		String procSQL ="select '/gallery/' || galId fqn, galCatId catId, filenm filename, filesize, width, height, typeCd, regUserId, to_char(regDate, 'yyyymmdd-hh24miss') regDate, subject, content " +
						" from gallery_tblc " +
						" where isRemoved= 'F'" ;
		
		Debug.line(saveToCraken(procSQL) + " applied");
	}
	
	public void testGalleryFile() throws Exception {
		final File[] files = FileUtil.findFiles(new File("./resource/uploadfiles/gallery"), FileFilterUtils.trueFileFilter(), true) ;
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				for (File file : files) {
					if (file.isDirectory()) continue ;
					if ( StringUtil.isNotBlank(FilenameUtils.getExtension(file.getName()))) continue ;
					
					DataInputStream input = new DataInputStream(new FileInputStream(file)) ;
					int size = input.readInt() ;
					
					byte[] metaByte = new byte[size] ;
					input.readFully(metaByte);
					JsonObject json = JsonObject.fromString(new String(metaByte, "UTF-8")) ;
					Debug.line(file.getName(), json);
					wsession.pathBy("/gallery/" + file.getName()).blob("file", input) ;
					
					IOUtil.close(input);
				}
				return null;
			}
		}) ;
	}

	
	public void testViewGallery() throws Exception {
//		session.pathBy("/gcat/sm").children().debugPrint(); 
		session.pathBy("/gallery").walkChildren().debugPrint();
	}
	
	
}
