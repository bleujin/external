package net.ion.external.domain;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.ion.cms.env.ICSFileSystem;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.StringUtil;
import net.ion.framework.vfs.VFile;

import org.apache.log4j.Logger;

public class DomainRSHanlder {

	private static Logger logger = Logger.getLogger(DomainReal.class);
	
	public final static ResultSetHandler<Void> contentIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/content", rs.getString("catid"), rs.getString("artid"))
						.property("artid", rs.getInt("artid")).property("subject", rs.getString("subject")).property("creuserid", rs.getString("creuserid"))
						.property("creday", rs.getString("creday")).property("moduserid", rs.getString("moduserid")).property("modday", rs.getString("modday"))
						.property("content", rs.getString("content")).property("operday", rs.getString("operday")).property("expireday", rs.getString("expireday"))
						.property("keywords", rs.getString("keywords"))
						.property("status", rs.getString("status"));
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}
	
	public final static  ResultSetHandler<Void> contentAfieldIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/content", rs.getString("catid"), rs.getString("artid"))
					.property(rs.getString("afieldid"), rs.getString("dvalue") + rs.getString("clobvalue")).property(rs.getString("afieldid") + "_type", rs.getString("typecd")) ;
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}
	
	
	public final static  ResultSetHandler<Void> contentThumbIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/content", rs.getString("catid"), rs.getString("artid")).property("thumbnail", rs.getString("thumbnail")) ;
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}

	public final static ResultSetHandler<Void> articleIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/article", rs.getString("catid"), rs.getString("artid"))
						.property("artid", rs.getInt("artid")).property("subject", rs.getString("subject")).property("creuserid", rs.getString("creuserid"))
						.property("creday", rs.getString("creday")).property("moduserid", rs.getString("moduserid")).property("modday", rs.getString("modday"))
						.property("content", rs.getString("content")).property("operday", rs.getString("operday")).property("expireday", rs.getString("expireday"))
						.property("keywords", rs.getString("keywords"))
						.property("useflg", rs.getString("useflg")).property("artfilenm", rs.getString("artfilenm")).property("status", rs.getString("status"));
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}
	
	public final static  ResultSetHandler<Void> articlAfieldIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/article", rs.getString("catid"), rs.getString("artid"))
						.property(rs.getString("afieldid"), rs.getString("dvalue") + rs.getString("clobvalue")).property(rs.getString("afieldid") + "_type", rs.getString("typecd")) ;
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}

	public final static  ResultSetHandler<Void> articleThumbIndexHandler(final WriteSession wsession) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0 ;
				while(rs.next()){
					wsession.pathBy("/article", rs.getString("catid"), rs.getString("artid")).property("thumbnail", rs.getString("thumbnail")) ;
					
					continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}


	public static void continueUnit(WriteSession wsession, int count) throws SQLException {
		if ((count % 1000) == 999)
			try {
				wsession.continueUnit();
			} catch (IOException e) {
				throw new SQLException(e);
			}
	}

	
	
	
	
	public final static ResultSetHandler<Void> articleMirrorHandler(final WriteSession wsession, final String[] domainFqns) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(final ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					WriteNode wnode = wsession.pathBy(rs.getString("fqn"));
					final String ncols = "artid,modserno,orderno,partid,priority" ;
					final String bcols = "isusingurlloc" ;
					for (String col : StringUtil.split(ncols, ",")) {
						wnode.property(col, rs.getInt(col));
					}
					for (String col : StringUtil.split(bcols, ",")) {
						wnode.property(col, rs.getBoolean(col));
					}
					String cols = ncols + "," + bcols;
					String[] dcols = StringUtil.split(cols, ",");
					for (Field field : Def.Article.class.getDeclaredFields()) {
						String colName = field.getName().toLowerCase();
						if (ArrayUtil.contains(dcols, colName) || "fqn".equals(colName))
							continue;
						wnode.property(colName, rs.getString(colName));
					}
					wnode.refTos("include", domainFqns);
					DomainRSHanlder.continueUnit(wsession, count++);
				}
				return null;
			}
		};
	}
	
	
	

	public final static ResultSetHandler<Void> articleImageMirrorHandler(final WriteSession wsession, final ICSFileSystem icsfs) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(final ResultSet rs) throws SQLException {
				int count = 0 ;
				while (rs.next()) {
					try {
                        boolean isThumbnail = rs.getBoolean("isthumbimg") ;
                        String fileLoc = rs.getString("imgFileLoc") ;
						VFile file = isThumbnail ? icsfs.thumbnailFile(fileLoc) : icsfs.artimageFile(fileLoc);

						if (!file.exists())
							continue;

                        wsession.pathBy(rs.getString("fqn")).blob(isThumbnail ? "thumbimg" : "img" + fileLoc.toLowerCase().hashCode(), file.getInputStream());
						DomainRSHanlder.continueUnit(wsession, count++);
					} catch (IOException ignore) {
						logger.warn(ignore.getMessage());
					}
				}
				return null;
			}
		};
	}

	public final static ResultSetHandler<Void> articleAfieldMirrorHandler(final WriteSession wsession, final ICSFileSystem icsfs) {
		return new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					try {
						WriteNode articleNode = wsession.pathBy(rs.getString("articleFqn"));
						articleNode.property(rs.getString("afieldid"), StringUtil.toString(rs.getString("dvalue")) + StringUtil.toString(rs.getString("clobvalue")));

						WriteNode afieldNode = wsession.pathBy(rs.getString("fqn"));
						Def.AfieldValue.Properties(afieldNode, rs);

						String typeCd = rs.getString("typeCd");
						if (("File".equals(typeCd) || "Image".equals(typeCd)) && StringUtil.isNotBlank(rs.getString("dvalue"))) {
							VFile file = icsfs.afieldFile(rs.getString("dvalue"));
							if (!file.exists())
								continue;
							afieldNode.blob("data", file.getInputStream());
						}
						DomainRSHanlder.continueUnit(wsession, count++);
					} catch (IOException ignore) {
						logger.warn(ignore.getMessage());
					}
				}
				return null;
			}
		};
	}
	
}
