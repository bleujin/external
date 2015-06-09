package net.ion.external.ics.bean;

import java.io.IOException;

import net.ion.craken.node.crud.ChildQueryRequest;
import net.ion.external.domain.Domain;
import net.ion.framework.util.StringUtil;


public class CategoryChildrenX<T extends AbCategory> {

	private Class<T> clz;
	private String expression;
	private Domain domain;
	private ChildQueryRequest request;

	private CategoryChildrenX(Domain domain, ChildQueryRequest request, Class<T> clz) {
		this.domain = domain;
		this.request = request ;
		this.clz = clz ;
	}

	public static CategoryChildrenX<SiteCategoryX> siteCategory(Domain domain, ChildQueryRequest request) {
		return new CategoryChildrenX<SiteCategoryX>(domain, request, SiteCategoryX.class);
	}
	
	public static CategoryChildrenX<GalleryCategoryX> galleryCategory(Domain domain, ChildQueryRequest request) {
		return new CategoryChildrenX<GalleryCategoryX>(domain, request, GalleryCategoryX.class);
	}
	
	public CategoryChildrenX<T> where(String expression){
		this.expression = expression ;
		return this ;
	}
	
	public XIterable<T> find() throws IOException {
		if (StringUtil.isNotBlank(expression)) request.where(expression) ;
		
		return XIterable.create(domain, request.find().toList(), clz);
	}
}
