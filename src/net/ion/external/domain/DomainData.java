package net.ion.external.domain;

import java.io.IOException;

import net.ion.craken.node.ReadNode;
import net.ion.external.ics.bean.AfieldMetaX;
import net.ion.external.ics.bean.ArticleChildrenX;
import net.ion.external.ics.bean.ArticleX;
import net.ion.external.ics.bean.CategoryChildrenX;
import net.ion.external.ics.bean.GalleryCategoryX;
import net.ion.external.ics.bean.GalleryChildrenX;
import net.ion.external.ics.bean.GalleryX;
import net.ion.external.ics.bean.SiteCategoryX;
import net.ion.external.ics.bean.TemplateChildrenX;
import net.ion.external.ics.bean.UserX;
import net.ion.nsearcher.search.filter.TermFilter;

public class DomainData {

	private Domain domain;
	DomainData(Domain domain) {
		this.domain = domain ;
	}

	private ReadNode ghostBy(String base, String... sub){
		return domain.ghostBy(base, sub) ;
	}
	
	private ReadNode domainNode(){
		return domain.domainNode() ;
	}
	
	
	public CategoryChildrenX<SiteCategoryX> scategorys() throws IOException {
		return CategoryChildrenX.siteCategory(domain, domainNode().refsToChildren("include").fqnFilter("/datas/scat")) ;
	}

	public CategoryChildrenX<GalleryCategoryX> gcategorys() throws IOException {
		return CategoryChildrenX.galleryCategory(domain, domainNode().refsToChildren("include").fqnFilter("/datas/gcat")) ;
	}
	
	public ArticleChildrenX articles() throws IOException {
		// /datas/article/{catid}/{artid}
		return ArticleChildrenX.create(domain, domainNode().refsToChildren("include").fqnFilter("/datas/article"));
	}
	
	public ArticleChildrenX articles(String catId) throws IOException {
		return ArticleChildrenX.create(domain, domainNode().refsToChildren("include").fqnFilter("/datas/article").filter(new TermFilter("catid", catId)));
	}

	public GalleryChildrenX gallerys() throws IOException {
		return GalleryChildrenX.create(domain, domainNode().refsToChildren("include").fqnFilter("/datas/gallery"));
	}

	public TemplateChildrenX templates() throws IOException {
		return TemplateChildrenX.create(domain, domainNode().refsToChildren("include").fqnFilter("/datas/template")) ;
	}

	
	

	
	
	public ArticleX article(String catId, int artId) {
		return ArticleX.create(domain, ghostBy("/datas/article/" + catId + "/" + artId));
	}

	
	public GalleryX findGallery(int galId) throws IOException{
		ReadNode found = domain.session().ghostBy("/datas/gallery").childQuery("galid:" + galId, true).findOne() ;
		if (found == null) {
			return GalleryX.create(domain, ghostBy("/datas/gallery/notfound/" + galId)) ;
		} return GalleryX.create(domain, found) ;
	}
	
    public GalleryX gallery(String gcatId, int galId) throws IOException {
        return GalleryX.create(domain, ghostBy("/datas/gallery/" + gcatId + "/" + galId)) ;
    }

    public SiteCategoryX scategory(String catId) {
		return SiteCategoryX.create(domain, ghostBy("/datas/scat", catId));
	}

	public GalleryCategoryX gcategory(String gcatId) {
		return GalleryCategoryX.create(domain, ghostBy("/datas/gcat", gcatId));
	}

	public AfieldMetaX afieldMeta(String afieldid) {
		return AfieldMetaX.create(domain, ghostBy("/datas/afield", afieldid));
	}

	public UserX findUser(String userId) {
		return UserX.create(domain,  ghostBy("/datas/user", userId));
	}


}
