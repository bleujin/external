package net.ion.cms.env;

import java.io.IOException;

import net.ion.craken.Craken;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.craken.node.crud.WorkspaceConfigBuilder;
import net.ion.external.ics.bean.AfieldX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.GalleryCategoryX;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.UserX;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.infinispan.manager.DefaultCacheManager;

public class ICSCraken {

	private Craken craken;

	public ICSCraken(Craken craken) throws IOException {
		 this.craken = craken ;
		 BooleanQuery.setMaxClauseCount(100000);
	}

	public ReadSession session() throws IOException {
		return craken.login() ;
	}

	public void unload() {
		craken.stop();
	}
	
	public void shutdown(){
		craken.stop();
	}

	public static ICSCraken create() throws IOException {
		return new ICSCraken(Craken.create());
	}
	
	public static ICSCraken test() throws IOException{
		return new ICSCraken(Craken.test()) ;
	}

	public SiteCategoryX findSiteCategory(String catId) throws IOException{
		return SiteCategoryX.create(this, session().ghostBy("/scat/" + catId)) ;
	}
	
	public GalleryCategoryX findGalleryCategory(String catId) throws IOException {
		return GalleryCategoryX.create(this, session().ghostBy("/gcat/" + catId)) ;
	}

	public UserX findUser(String userId) throws IOException {
		return UserX.create(this, session().ghostBy("/user/" + userId)) ;
	}

	public AfieldX findAfield(String afieldId) throws IOException {
		return AfieldX.create(this, session().ghostBy("/afield/" + afieldId));
	}

}
