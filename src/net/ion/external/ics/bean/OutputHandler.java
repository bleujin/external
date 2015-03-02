package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.xml.XML;

import net.ion.craken.tree.PropertyId;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.StringBuilderWriter;
import net.ion.framework.util.StringUtil;

public abstract class OutputHandler {

	private Writer inner;
	protected OutputHandler(Writer inner) {
		this.inner = inner ;
	}

	
	public static OutputHandler createJson(Writer writer) {
		return new JsonOutputHandler(writer);
	}
	public static OutputHandler createXml(Writer writer) {
		return new XmlOutputHandler(writer);
	}

	public static OutputHandler createHtml(Writer writer) {
		return new HtmlOutputHandler(writer);
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

	protected JsonOutputHandler(Writer inner) {
		super(inner) ;
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		JsonWriter jwriter = new JsonWriter(inner()) ;
		
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

	protected XmlOutputHandler(Writer inner) {
		super(inner);
	}

	@Override
	public <T extends BeanX> OutputHandler out(Writable we, JsonObject request, JsonObject response, String... fields) throws IOException {
		//<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n
		// <result> <request><property name="pid">value</property></request> <nodes><node><property type="String" name="catid"><![CDATA[bleujin]]></property>...</node>...</nodes>
		inner().write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n");
		XML root = new XML("result");
		root.setPrettyPrint(true);
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
		}
		return xml ;
	}
	
}
