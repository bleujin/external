package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.impl.AvalonLogger;
import org.apache.lucene.analysis.kr.utils.StringUtil;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;
import net.ion.framework.util.DateUtil;

public class AfieldMetaX extends BeanX{

	public enum Type {
		Number{
			public Long asObject(AfieldValueX avalue){
				return avalue.asLong() ;
			}
		}, 
		Date {
			public java.util.Date asObject(AfieldValueX avalue) throws IOException  {
				try {
					if (StringUtil.isBlank(avalue.asString())) return DateUtil.stringToDate("2000-01-01") ;
					else return DateUtil.stringToDate(avalue.asString()) ;
				} catch(ParseException e){
					throw new IOException(e) ;
				}
			}
		}, 
		File {
			public InputStream asObject(AfieldValueX avalue) throws IOException{
				return avalue.asStream() ;
			}
		}, 
		Image {
			public InputStream asObject(AfieldValueX avalue) throws IOException{
				return avalue.asStream() ;
			}
		}, 
		LongString, 
		Currency {
			public Long asObject(AfieldValueX avalue){
				return avalue.asLong() ;
			}
		}, 
		Summary,Editor, String, Boolean {
			public Boolean asObject(AfieldValueX avalue){
				return avalue.asBoolean() ;
			}
		}, 
		
		Set {
			public Map<String, Object> asObject(AfieldValueX avalue) throws IOException{
				return avalue.asMap() ;
			}
		} ;

		public Object asObject(AfieldValueX avalue) throws IOException {
			return avalue.asString();
		}
	}
	

	public AfieldMetaX(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public static AfieldMetaX create(Domain domain, ReadNode node) {
		return new AfieldMetaX(domain, node);
	}

	public String afieldId() {
		return asString(Def.Afield.AfieldId);
	}
	
	public String typeCd() {
		return asString(Def.Afield.TypeCd);
	}
	
	public Type type(){
		return Type.valueOf(typeCd()) ;
	}

	public XIterable<AfieldMetaX> children() {
		return XIterable.create(domain(), node().walkRefChildren("tree").toList(), AfieldMetaX.class);
	}	

}
