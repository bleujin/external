package net.ion.external.ics.common;

import com.google.common.base.Function;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.util.TraversalStrategy;
import net.ion.framework.parse.gson.JsonObject;

import java.util.Iterator;

public class Trans {

	public static final Function<ReadNode, JsonObject> DECENT = new Function<ReadNode, JsonObject>(){
		@Override
		public JsonObject apply(ReadNode target) {
			final JsonObject result = new JsonObject() ;
		
			target.walkChildren().asTreeChildren().includeSelf(true).strategy(TraversalStrategy.BreadthFirst).transform(new Function<Iterator<ReadNode>, Void>() {
				@Override
				public Void apply(Iterator<ReadNode> decent) {
					while(decent.hasNext()){
						ReadNode node = decent.next() ;
						result.add(node.fqn().toString(), node.toValueJson());
					}
					return null;
				}
			}) ;
			
			return result;
		}
	};
	
}
