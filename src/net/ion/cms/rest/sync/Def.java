package net.ion.cms.rest.sync;

public class Def {

	public final static class Category {
		// catId, upperCatId, catNm, phyDirNm, useCd, catExp, charSetCd, catUrl, orderLnNo, fileServerUrl, isRemoved, sentinelExclude, userss
		public final static String CatId = "catid";
		public final static String UpperCatId = "upppercatid";
		public final static String CatNm = "catnm";
		public final static String PhyDirNm = "phydirnm";
		public final static String UseCd = "usecd";
		public final static String CatExp = "catexp";
		public final static String CharSetCd = "charsetcd";
		public final static String CatUrl = "caturl";
		public final static String OrderLnNo = "orderlnno";
		public final static String FileServerUrl = "fileserverurl";
		public final static String IsRemoved = "isremoved";
		public final static String SentinelExclude = "sentinelexclude";
		public final static String UseRss = "userss";
	}

	public final static class GalleryCategory {
		public final static String CatId = "catid";
		public final static String UpperCatId = "upppercatid";
		public final static String CatNm = "catnm";
	}

	public final static class Gallery {
		public final static String GalId = "galid";
		public final static String CatId = "catid";
		public final static String FileNm = "filenm";
		public final static String FileSize = "filesize";
		public final static String Width = "width";
		public final static String Height = "height";
		public final static String TypeCd = "typecd";
		public final static String FileMeta = "filemeta";
		public final static String IsRemoved = "isremoved";
		public final static String Subject = "subject";
		public final static String Content = "content";
	}

	public final static class Article {
		public final static String CatId = "catid";
		public final static String OperDay = "operday";
		public final static String ExpireDay = "expireday";
		public final static String ArtId = "artid";
		public final static String ModSerNo = "modserno";
		public final static String OrderNo = "orderno";
		public final static String UseFlg = "useflg";
		public final static String ArtFileNm = "artfilenm";
		public final static String PartId = "partid";
		public final static String Priority = "priority";
		public final static String Action = "action";
		public final static String ModUserId = "moduserid";
		public final static String IsUsingUrlLoc = "isusingurlloc";
		public final static String Keyword = "keyword";
		public final static String ArtCont = "artcont";
		public final static String GoUrlLoc = "gourlloc";
		public final static String RegUserId = "reguserid";
		public final static String Thumnail = "thumnail";
		public final static String ArtSubject = "artsubject";
		public final static String ModDay = "modday";
		public final static String CreDay = "creday";
		public static final String Related = "related";
	}
	
	public final static class Template {
		public final static String TplId = "tplid" ;
		public final static String CatId = "catid" ;
		public final static String ListFileNm = "listfilenm" ;
		public final static String TplKindCd = "tplkindcd" ;
		public final static String TplTypeCd = "tpltypecd" ;
		public final static String TplNm = "tplnm" ;
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
	};
}
