package net.ion.cms.rest.sync;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.logging.annotations.Pos;

import oracle.net.aso.n;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;

public class Def {

	public final static class Category {
		// catId, upperCatId, catNm, phyDirNm, useCd, catExp, charSetCd, catUrl, orderLnNo, fileServerUrl, isRemoved, sentinelExclude, userss
		public final static String CatId = "catid";
		public final static String Parent = "parent";
		public final static String Name = "name";
		public final static String PhyDirNm = "phydirnm";
		public final static String Explain = "explain";
		public final static String CharSetCd = "charsetcd";
		public final static String CatUrl = "caturl";
		public final static String OrderLnNo = "orderlnno";
		public final static String FileServerUrl = "fileserverurl";
	}

	public final static class GalleryCategory {
		public final static String CatId = "catid";
		public final static String Parent = "parent";
		public final static String Name = "name";
		public final static String RegUserId = "reguserid";
	}

	public final static class Gallery {
		public final static String GalId = "galid";
		public final static String CatId = "catid";
		public final static String FileName = "filename";
		public final static String FileSize = "filesize";
		public final static String Width = "width";
		public final static String Height = "height";
		public final static String TypeCd = "typecd";
		public final static String FileMeta = "filemeta";
		public final static String Subject = "subject";
		public final static String Content = "content";
		public final static String RegUserId = "reguserid";
		
		public static void Properties(WriteNode wnode, ResultSet rs) throws SQLException {
			String[] nprops = new String[]{GalId, FileSize, Width, Height} ;
			for(String prop : nprops){
				wnode.property(prop, rs.getInt(prop)) ;
			}
			String[] sprops = new String[]{CatId, FileName, TypeCd, FileMeta, Subject, Content, RegUserId} ;
			for(String prop : sprops){
				wnode.property(prop, rs.getString(prop)) ;
			}
		}

	}

	public final static class Article {
		
		public final static String CatId = "catid";
		public final static String OperDay = "operday";
		public final static String ExpireDay = "expireday";
		public final static String ArtId = "artid";
		public final static String ModSerNo = "modserno";
		public final static String OrderNo = "orderno";
		public final static String ArtFileNm = "artfilenm";
		public final static String PartId = "partid";
		public final static String Priority = "priority";
		public final static String Action = "action";
		public final static String ModUserId = "moduserid";
		public final static String IsUsingUrlLoc = "isusingurlloc";
		public final static String Keyword = "keyword";
		public final static String Content = "content";
		public final static String GoUrlLoc = "gourlloc";
		public final static String RegUserId = "reguserid";
		public final static String Subject = "subject";
		public final static String ModDay = "modday";
		public final static String CreDay = "creday";
//		public static final String Related = "related";
	}
	
	public final static class Template {
		public final static String TplId = "tplid" ;
		public final static String CatId = "catid" ;
		public final static String FileName = "filename" ;
		public final static String KindCd = "kindcd" ;
		public final static String TypeCd = "typecd" ;
		public final static String Name = "name" ;
		public final static String Explain = "explain" ;
		public final static String RegUserId = "reguserid" ;
	}
	
	public final static class Afield {
		public final static String AfieldId = "afieldid";
		public final static String TypeCd = "typecd";
		public final static String AfieldNm = "afieldnm";
	}

	public final static class User  {
		public final static String UserId = "userid" ;
        public final static String Name = "usernm";
        public final static String Pwd = "pwd" ;
		public final static String VerifyKey = "verifykey" ;
		public final static String RetireDay = "retireday" ;
		public final static String EnroleDay = "enroleday";
	}

	public static final class AfieldValue {
		public final static String CatId = "catid" ;
		public final static String ArtId = "artid" ;
		public final static String  AfieldId = "afieldid" ;
		public final static String TypeCd = "typecd" ;
		public final static String StringValue = "stringvalue" ;

		public static void Properties(WriteNode afieldNode, ResultSet rs) throws SQLException {
			afieldNode.property(CatId, rs.getString(CatId)).property(ArtId, rs.getInt(ArtId))
				.property(AfieldId, rs.getString(AfieldId))
				.property(TypeCd, rs.getString(TypeCd))
				.property(StringValue, rs.getString("dvalue") + rs.getString("clobvalue")) ;
		}
		
	}
}
