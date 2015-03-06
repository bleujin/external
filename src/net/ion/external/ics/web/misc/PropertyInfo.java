package net.ion.external.ics.web.misc;

import net.ion.framework.parse.gson.JsonObject;

import java.util.Map.Entry;
import java.util.Properties;

public class PropertyInfo {
	public JsonObject list() {
		JsonObject result = new JsonObject();
		Properties props = System.getProperties();
		for (Entry<Object, Object> entry : props.entrySet()) {
			result.put(entry.getKey().toString(), entry.getValue());
		}
		return result;
	}
}
