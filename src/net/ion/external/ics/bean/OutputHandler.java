package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.Writer;

import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.StringUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.xml.XML;

public abstract class OutputHandler {

	private Writer inner;
	protected OutputHandler(Writer inner) {
		this.inner = inner ;
	}

	
	public static OutputHandler createJson(Writer writer, boolean indent) {
		return new JsonOutputHandler(writer, indent);
	}
	public static OutputHandler createXml(Writer writer, boolean indent) {
		return new XmlOutputHandler(writer, indent);
	}

	public static OutputHandler createHtml(Writer writer) {
		return new HtmlOutputHandler(writer);
	}


	public static OutputHandler createCsv(Writer writer) {
		return new CsvOutputHandler(writer);
	}


	
	public abstract <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException ;
	
	public void debugPrint() {
		Debug.line(inner.toString());
	}
	
	public void close(){
		IOUtil.close(inner);
	}
	
	protected Writer inner(){
		return inner ;
	}

}


class CsvOutputHandler extends OutputHandler {
	
	protected CsvOutputHandler(Writer inner) {
		super(inner) ;
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		Writer writer = inner() ;
		we.csvSelf(writer, fields);
		return this;
	}
}

class HtmlOutputHandler extends OutputHandler {

	protected HtmlOutputHandler(Writer inner) {
		super(inner) ;
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		
		Writer writer = inner() ;
		writer.append("<html>\r\n") ;
		writer.append("<head>\r\n") ;
		writer.append("</head>\r\n") ;
		writer.append("<body>\r\n") ;
		
		writer.append("<table>\r\n") ;
		
		we.htmlSelf(inner(), fields);
		
		writer.append("</table>\r\n") ;
		writer.append("</body>\r\n") ;
		writer.append("</html>\r\n") ;
		
		return this ;
	}
	
}


class JsonOutputHandler extends OutputHandler {

	private boolean indent;
	protected JsonOutputHandler(Writer inner, boolean indent) {
		super(inner) ;
		this.indent = indent ;
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		JsonWriter jwriter = new JsonWriter(inner()) ;
		if (indent) jwriter.setIndent("\t");
		
		jwriter
		.beginObject().name("result") 
			.beginObject()
				.jsonElement("request", request)
				.jsonElement("response", response)
				.name("nodes")
				.beginArray() ;
	
				we.jsonSelf(jwriter, fields);
				
				jwriter.endArray()
			.endObject() 
		.endObject() ;
		jwriter.flush();
		return this ;
	}
	
	public void debugPrint() {
		new GsonBuilder().setPrettyPrinting().create().toJson(JsonObject.fromString(inner().toString()), System.out);
	}
}

class XmlOutputHandler extends OutputHandler {

	private boolean indent;

	protected XmlOutputHandler(Writer inner, boolean indent) {
		super(inner);
		this.indent = indent ;
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		//<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n
		// <result> <request><property name="pid">value</property></request> <nodes><node><property type="String" name="catid"><![CDATA[bleujin]]></property>...</node>...</nodes>
		inner().write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n");
		XML root = new XML("result");
		if (indent) root.setPrettyPrint(true);
		root.addElement(toXML("request", request));
		root.addElement(toXML("response", response));

		XML xmlNodes = new XML("nodes");
		xmlNodes.setPrettyPrint(true);
		root.addElement(xmlNodes);

		we.xmlSelf(xmlNodes, fields);

		root.output(inner());
		return this;
	}
	
	private XML toXML(String name, JsonObject json) {
		XML xml = new XML(name);
		xml.setPrettyPrint(true);
		for(String key : json.keySet()) {
			XML el = new XML("property");
			el.addAttribute("name", key);
			el.addElement(StringEscapeUtils.escapeXml(StringUtil.toString(json.asString(key), "")));
			xml.addElement(el) ;
		}
		return xml ;
	}
	
}
