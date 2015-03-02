package net.ion.external.ics.bean;

import net.ion.craken.node.crud.ReadChildren;
import net.ion.framework.util.StringUtil;


public class CategoryChildrenX<T extends AbCategory> {

	private T from;
	private boolean includeLeaf;
	private Class<T> clz;
	private String expression;

	private CategoryChildrenX(T from, boolean includeLeaf, Class<T> clz) {
		this.from = from;
		this.includeLeaf = includeLeaf ;
		this.clz = clz ;
	}

	public static CategoryChildrenX<SiteCategoryX> siteCategory(SiteCategoryX from, boolean includeLeaf) {
		return new CategoryChildrenX<SiteCategoryX>(from, includeLeaf, SiteCategoryX.class);
	}
	
	public static CategoryChildrenX<GalleryCategoryX> galleryCategory(GalleryCategoryX from, boolean includeLeaf) {
		return new CategoryChildrenX<GalleryCategoryX>(from, includeLeaf, GalleryCategoryX.class);
	}
	
	public CategoryChildrenX<T> where(String expression){
		this.expression = expression ;
		return this ;
	}
	
	public T from(){
		return from ;
	}

	public XIterable<T> find() {
		ReadChildren list = includeLeaf ? from.node().walkRefChildren("tree") : from.node().refChildren("tree") ;
		if (StringUtil.isNotBlank(expression)) list.where(expression);
		
		return XIterable.create(from.rc(), list.toList(), clz);
	}
}
