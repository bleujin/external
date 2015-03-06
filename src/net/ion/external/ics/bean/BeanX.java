package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.convert.Functions;
import net.ion.craken.tree.Fqn;
import net.ion.craken.tree.PropertyId;
import net.ion.craken.tree.PropertyValue;
import net.ion.craken.tree.PropertyValue.VType;
import net.ion.external.domain.Domain;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.StringUtil;

import org.apache.ecs.xml.XML;

import com.google.common.base.Function;

public abstract class BeanX {

	private ReadNode node;
	private Domain domain;

	public static InputStream BLANKSTREAM = new InputStream() {
		@Override
		public int read() throws IOException {
			return -1;
		}
	}; 
	
	public BeanX(Domain domain, ReadNode node) {
		this.domain = domain;
		this.node = node;
	}

	protected ReadNode node() {
		return node;
	}

	protected Domain domain() {
		return domain;
	}

	public boolean exists(){
		return ! node.isGhost() ;
	}
	
	protected ReadSession session() {
		return node.session();
	}

	protected PropertyValue property(String propId) {
		return node.property(propId);
	}

	public int asInt(String propId) {
		return node.property(propId).asInt();
	}

	public String asString(String propId) {
		return node.property(propId).asString();
	}
	
	public String asString(String propId, String dftString) {
		return node.property(propId).defaultValue(dftString);
	}

	public boolean hasProperty(String pid) {
		return node().hasProperty(pid);
	}
	public InputStream asStream(String propId) throws IOException{
		if (! hasProperty(propId)) return BLANKSTREAM ;
		return node.property(propId).asBlob().toInputStream() ;
	}

	public boolean asBoolean(String propId) {
		return ArrayUtil.contains(new String[] { "T", "true" }, asString(propId));
	}

	public ReadNode pathBy(String path) {
		return node.session().ghostBy(path);
	}

	public ReadNode pathBy(Fqn path) {
		return node.session().ghostBy(path);
	}

	public OutputHandler out(OutputHandler ohandler, String... fields) throws IOException {
		return ohandler.out(toWritable(), new JsonObject(), new JsonObject(), fields);
	}

	public void debugPrint(){
		Debug.line(node, node.transformer(Functions.READ_TOFLATMAP));
	}
	
	public Writable toWritable() {
		final ReadNode readNode = node();

		return new Writable() {
			
			private Set<PropertyId> targetPropKeys(ReadNode node, String... fields){
				if (fields == null || fields.length == 0) return node.keys() ;
				Set<PropertyId> result = SetUtil.newSet() ;
				
				for (String field : fields) {
					result.add(PropertyId.normal(field)) ;
				}
				
				return result ;
			}
			
			@Override
			public void jsonSelf(final JsonWriter jwriter, final String... fields) throws IOException {
				jwriter.beginObject();

				readNode.transformer(new Function<ReadNode, Void>() {
					@Override
					public Void apply(ReadNode node) {
						try {
							
							for (PropertyId pid : targetPropKeys(node, fields)) {
								PropertyValue pvalue = node.propertyId(pid);
								jwriter.name(pid.idString());

								if (pvalue.type() == VType.INT || pvalue.type() == VType.LONG) {
									jwriter.value(pvalue.asLong(0));
								} else if (pvalue.type() == VType.DOUB) {
									jwriter.value(pvalue.asDouble(0));
								} else if (pvalue.type() == VType.BOOL) {
									jwriter.value(pvalue.asBoolean());
								} else {
									jwriter.value(pvalue.asString());
								}
							}
						} catch (IOException ignore) {

						}
						return null;
					}
				});

				jwriter.endObject();
			}

			@Override
			public void xmlSelf(XML parent, final String... fields) throws IOException {
				final XML xnode = new XML("node");
				parent.addElement(xnode);

				readNode.transformer(new Function<ReadNode, Void>() {
					@Override
					public Void apply(ReadNode node) {
						for (PropertyId pid : targetPropKeys(node, fields)) {
							PropertyValue pvalue = node.propertyId(pid);
							XML prop = new XML("property");
							xnode.addElement(prop);
							prop.addAttribute("name", pid.idString());
							prop.addAttribute("type", pvalue.type());
							prop.addElement(getCDataSection(pvalue.asString()));
						}
						return null;
					}

					private String getCDataSection(String val) {
						if (StringUtil.isBlank(val))
							return "";
						else
							return (new StringBuilder("<![CDATA[")).append(val).append("]]>").toString();
					}
				});
			}

			@Override
			public void htmlSelf(final Writer writer, final String... fields) throws IOException {
				readNode.transformer(new Function<ReadNode, Void>() {
					@Override
					public Void apply(ReadNode node) {
						try {
							writer.append("<tr>");
							for (PropertyId pid : targetPropKeys(node, fields)) {

								writer.append("<td>");
								PropertyValue pvalue = node.propertyId(pid);
								
								writer.append(pid.idString() + ":" +  pvalue.asString());
								writer.append("</td>");
							}
							writer.append("</tr>\r\n");
						} catch (IOException ignore) {
						}
						return null;
					}
				});
			}
		};
	}
}
