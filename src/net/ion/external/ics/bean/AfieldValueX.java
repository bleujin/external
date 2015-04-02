package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.ListUI;

import org.apache.lucene.analysis.kr.utils.StringUtil;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.craken.tree.Fqn;
import net.ion.external.domain.Domain;
import net.ion.external.ics.bean.AfieldMetaX.Type;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.NumberUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;

public class AfieldValueX extends BeanX{

	private String catId ;
	private int artId ;
	private AfieldValueX(Domain domain, ReadNode node, String catId, int artId) {
		super(domain, node) ;
		this.catId = catId ;
		this.artId = artId ;
	}

	public static AfieldValueX create(Domain domain, ReadNode node) {
		return new AfieldValueX(domain, node, node.property("catid").asString(), node.property("artid").asInt());
	}

	public static AfieldValueX create(Domain domain, ReadNode node, String catId, int artId) {
		return new AfieldValueX(domain, node, catId, artId);
	}
	
	
	public String typeCd() {
		return meta().typeCd();
	}
	
	public AfieldMetaX meta(){
		return AfieldMetaX.create(domain(), session().ghostBy(Def.Afield.pathBy(node().fqn().name()))) ;
	}
	
	public ArticleX article() {
		return ArticleX.create(domain(), session().ghostBy(Def.Article.pathBy(catId, artId)));
	}


	public InputStream dataStream() throws IOException {
		return super.asStream("data") ;
	}

	public boolean asBoolean() {
		return "T".equals(asString()) ;
	}

	public String asString() {
		return super.asString("stringvalue") ;
	}
	
	public InputStream asStream() throws IOException{
		return dataStream() ;
	}
	
	public int asInt(){
		return NumberUtil.toInt(asString(), 0) ;
	}

	public long asLong() {
		return NumberUtil.toLong(asString(), 0L);
	}

	public Map<String, Object> asMap() throws IOException {
		Map<String, Object> result = MapUtil.newMap() ;
		XIterable<AfieldMetaX> children = meta().children() ;
		for(AfieldMetaX am : children){
			result.put(am.afieldId(), article().asAfield(am.afieldId()).asObject()) ;
		}
		
		return result;
	}

	
	public int asInt(String propId) {
		return asInt() ;
	}

	public String asString(String propId) {
		return asString() ;
	}
	
	public String asString(String propId, String dftString) {
		return StringUtil.defaultIfEmpty(asString(), dftString) ;
	}

	public InputStream asStream(String propId) throws IOException{
		return asStream() ;
	}

	public boolean asBoolean(String propId) {
		return asBoolean() ;
	}

	public Object asObject() throws IOException {
		Type type = type() ;
		return type.asObject(this) ;
	}

	private Type type() {
		return meta().type();
	}

	public String afieldId() {
		return node().fqn().name();
	}

	public String asStreamPath() {
		return "afield/" + catId + "/" + artId + "/" + afieldId() + ".stream";
	}

}
