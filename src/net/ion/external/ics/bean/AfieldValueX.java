package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;

import net.ion.craken.node.ReadNode;
import net.ion.external.domain.Domain;
import net.ion.framework.util.NumberUtil;

public class AfieldValueX extends BeanX{

	private String afieldId;

	private AfieldValueX(Domain domain, ReadNode node) {
		super(domain, node) ;
	}

	public static AfieldValueX create(Domain domain, ReadNode node) {
		return new AfieldValueX(domain, node);
	}

	public String typeCd() {
		return super.asString("typecd");
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
	
	public int asInt(){
		return NumberUtil.toInt(asString(), 0) ;
	}

	
	public int asInt(String propId) {
		throw new UnsupportedOperationException();
	}

	public String asString(String propId) {
		throw new UnsupportedOperationException();
	}
	
	public String asString(String propId, String dftString) {
		throw new UnsupportedOperationException();
	}

	public InputStream asStream(String propId) throws IOException{
		throw new UnsupportedOperationException();
	}

	public boolean asBoolean(String propId) {
		throw new UnsupportedOperationException();
	}


}
