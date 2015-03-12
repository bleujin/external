package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.convert.Functions;
import net.ion.craken.node.convert.Predicates;
import net.ion.external.domain.Domain;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;

import org.apache.ecs.xml.XML;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class XIterable<T extends BeanX> implements Iterable<T>, Writable {

	private Domain domain;
	private List<ReadNode> tnodes = null;
	private Map<String, String> param;
	private Class<T> clz = null;

	public XIterable(Domain domain, List<ReadNode> tnodes, Map<String, String> param, Class<T> clz) {
		this.domain = domain;
		this.tnodes = tnodes;
		this.param = param;
		this.clz = clz;
	}

	public static <T extends BeanX> XIterable<T> create(Domain domain, List<ReadNode> tnodes, Map<String, String> param, Class<T> clz) {
		return new XIterable<T>(domain, tnodes, param, clz);
	}

	public static <T extends BeanX> XIterable<T> create(Domain domain, List<ReadNode> tnodes, Class<T> clz) {
		return new XIterable<T>(domain, tnodes, MapUtil.<String, String> newMap(), clz);
	}

	public XIterable<T> match(String expression) {
		Iterable<ReadNode> newiter = Iterables.filter(tnodes, Predicates.where(expression));
		List<ReadNode> newlist = ListUtil.toList(Iterables.toArray(newiter, ReadNode.class));
		return new XIterable<T>(domain, newlist, param, clz);
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<ReadNode> iterator = tnodes.iterator();

		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				try {
					return (T) clz.getMethod("create", Domain.class, ReadNode.class).invoke(null, domain, iterator.next());
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public void remove() {
			}
		};
	}

	public <X> X each(Function<Iterator<T>, X> fn) {
		return fn.apply(iterator());
	}

	@Override
	public void jsonSelf(JsonWriter jwriter, String... fields) throws IOException {
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			iter.next().toWritable().jsonSelf(jwriter, fields);
		}
	}

	@Override
	public void xmlSelf(XML nodes, String... fields) throws IOException {
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			iter.next().toWritable().xmlSelf(nodes, fields);
		}
	}

	@Override
	public void htmlSelf(Writer writer, String... fields) throws IOException {
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			iter.next().toWritable().htmlSelf(writer, fields);
		}
	}

	@Override
	public void csvSelf(Writer writer, String... fields) throws IOException {
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			iter.next().toWritable().csvSelf(writer, fields);
		}
	}


	
	public OutputHandler out(OutputHandler ohandler, String... fields) throws IOException {
		JsonObject request = JsonObject.fromObject(param);
		JsonObject response = new JsonObject();

		return ohandler.out(this, request, response, fields);
	}

	public Set<String> paramKeys(){
		return param.keySet() ;
	}
	
	public String param(String name) {
		return param.get(name);
	}

	public void debugPrint() {
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			T t = iter.next();
			Debug.line(t.node(), t.node().transformer(Functions.READ_TOFLATMAP));
		}
	}

	public int count() {
		int result = 0;
		Iterator<T> iter = iterator();
		while (iter.hasNext()) {
			T t = iter.next();
			result++;
		}
		return result;
	}

	public XIterable<T> ascending(final String name) {
		Collections.sort(tnodes, new Comparator<ReadNode>() {

			@Override
			public int compare(ReadNode o1, ReadNode o2) {
				return o1.property(name).compareTo(o2.property(name));
			}
		});
		return this;
	}
	
	public List<T> toList(){
		return Lists.newArrayList(iterator()) ;
	}

	public XIterable<T> reverse() {
		Collections.reverse(tnodes);

		return this;
	}

	public boolean hasKey(Object fqnName){
		for (ReadNode node : tnodes) {
			if (ObjectUtil.toString(fqnName).equals(node.fqn().name())) {
				return true ;
			}
		}
		return false ;
	}
	
	public T findByKey(Object fqnName) throws IOException {
		try {
			for (ReadNode node : tnodes) {
				if (ObjectUtil.toString(fqnName).equals(node.fqn().name())) {
					return (T) clz.getMethod("create", Domain.class, ReadNode.class).invoke(null, domain, node);
				}
			}
			return (T) clz.getMethod("create", Domain.class, ReadNode.class).invoke(null, domain, domain.session().ghostBy("/notfound"));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
