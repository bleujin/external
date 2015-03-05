package net.ion.external.ics.bean;

import java.io.IOException;
import java.util.List;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.ListUtil;

import org.apache.lucene.analysis.kr.utils.StringUtil;

public class AbCategory<T extends AbCategory> extends BeanX{

	private String joiner = "/";
	public AbCategory(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public boolean exists(){
		return ! node().isGhost() ;
	}
	
	public String catId() {
		return asString(Def.Category.CatId);
	}

	public T parent() throws IOException {
		return (T) domain().scategory(asString("parent"));
	}

	public T joiner(String joiner) {
		this.joiner = joiner;
		return (T) this;
	}
	
	public String pathById() throws IOException {
		List<String> names = ListUtil.newList();
		AbCategory<T> current = this ;
		while(StringUtil.isNotBlank(current.parent().catId())){
			names.add(0, current.catId()) ;
			current = current.parent() ;
		}
		return joiner + StringUtil.join(names, joiner);
	}
	
	public String pathByName() throws IOException {
		List<String> names = ListUtil.newList();
		AbCategory<T> current = this ;
		while(StringUtil.isNotBlank(current.parent().asString("catid"))){
			
			names.add(0, current.asString(Def.Category.Name));
			current = current.parent();
		}
		
		return joiner + StringUtil.join(names, joiner);
	}
	

	public void jsonSelf(JsonWriter jwriter) throws IOException{
		jwriter.beginObject()
				.name("catid").value(catId())
			.endObject() ;
	}

	
	public boolean equals(Object obj){
		if (! (obj instanceof AbCategory)) return false ;
		AbCategory that = (AbCategory) obj ;
		return this.node().equals(that.node()) ;
	}
	
	
	public int hashCode(){
		return node().hashCode() + 17 ;
	}
	
	public String toString(){
		return getClass().getSimpleName() + ":" + node() ;
	}


}
