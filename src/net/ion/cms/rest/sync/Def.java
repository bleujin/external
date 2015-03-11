package net.ion.cms.rest.sync;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ion.craken.node.WriteNode;
import net.ion.craken.tree.Fqn;

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
//		public final static String CatPath = "catpath" ;
	}

	public final static class GalleryCategory {
		public final static String CatId = "catid";
		public final static String Parent = "parent";
		public final static String Name = "name";
		public final static String RegUserId = "reguserid";
//		public final static String CatPath = "catpath" ;
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
		
		public static Fqn pathBy(String catId, int artId) {
			return Fqn.fromString("/datas/article/" + catId + "/" + artId);
		}
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
		
		public static Fqn pathBy(String catId, int tplId){
			return Fqn.fromString("/datas/template/" + catId + "/" + tplId) ;
		}
		
	}
	
	public final static class Afield {
		public final static String AfieldId = "afieldid";
		public final static String Name = "name";
		public final static String Explain = "explain" ;
		public final static String TypeCd = "typecd";
		public final static String IsMndt = "ismndt" ;
		public final static String ExamId = "examid" ;
		public final static String DefaultValue = "defaultvalue" ;

		public static void Properties(WriteNode wnode, ResultSet rs) throws SQLException {
			String[] sprops = new String[]{AfieldId, Name, Explain, TypeCd, IsMndt, ExamId, DefaultValue} ;
			for(String prop : sprops){
				wnode.property(prop, rs.getString(prop)) ;
			}
		}
		
		public static Fqn pathBy(String afieldId){
			return Fqn.fromString("/datas/afield/" + afieldId) ;
		}
	}

	public final static class User  {
		public final static String UserId = "userid" ;
		public final static String Name = "name" ;
		public final static String Password = "password" ;
		public final static String VerifyKey = "verifykey" ;
		public final static String RetireDay = "retireday" ;
		public final static String EnroleDay = "enroleday";
		public static void Properties(WriteNode wnode, ResultSet rs) throws SQLException {
			wnode.property(UserId, rs.getString(UserId)).property(Name, rs.getString(Name)).property(Password, rs.getString(Password)) ;
		}
		public static Fqn pathBy(String userId) {
			return Fqn.fromString("/datas/user/" + userId);
		}
	}

	public static final class AfieldValue {
		public final static String CatId = "catid" ;
		public final static String ArtId = "artid" ;
		public final static String  AfieldId = "afieldid" ;
		public final static String TypeCd = "typecd" ;
		public final static String StringValue = "stringvalue" ;

		public static void Properties(WriteNode afieldNode, ResultSet rs) throws SQLException {
			afieldNode
				.property(CatId, rs.getString(CatId))
				.property(ArtId, rs.getInt(ArtId))
				.property(AfieldId, rs.getString(AfieldId))
				.property(TypeCd, rs.getString(TypeCd))
				.property(StringValue, rs.getString("dvalue") + rs.getString("clobvalue")) ;
		}
		
	}
	

	public static class Script {
		public static final String Sid = "sid" ;
		public static final String Content = "content" ;
		public static final String Running = "running" ;
	}
	

	public static class Schedule {
		public static final String Sid = "sid" ;
		public static final String MINUTE = "minute" ;
		public static final String HOUR = "hour" ;
		public static final String DAY = "day" ;
		public static final String MONTH = "month" ;
		public static final String WEEK = "week" ;
		public static final String MATCHTIME = "matchtime" ;
		public static final String YEAR = "year" ;
		
		public static final String ENABLE = "enable" ;
		public static final String Parity = "parity";
	}
	
	public static class SLog {
		public static final String CIndex = "cindex" ;
		public static final String Sid = "sid" ;
		public static final String Runtime = "runtime" ;
		public static final String Status = "status" ;
		public static final String Success = "success";
		public static final String Fail = "fail";
		public static final String Result = "result";
		
		public static String path(String sid){
			return "/scripts/" + sid + "/slogs" ;
		}
	}
}
