package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.Writer;

import org.apache.ecs.xml.XML;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;

public interface Writable {

	public void jsonSelf(JsonWriter jwriter, String... fields) throws IOException ;
	public void xmlSelf(XML xmlNodes, String... fields) throws IOException ;
	public void htmlSelf(Writer writer, String... fields) throws IOException ;
}
