package net.ion.external.ics.common;

import net.ion.nradon.*;
import net.ion.nradon.handler.event.ServerEvent.EventType;

public class FavIconHandler implements HttpHandler {

	
	@Override
	public void onEvent(EventType eventtype, Radon radon) {
	}

	@Override
	public int order() {
		return 0;
	}

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
		response.status(404).end() ;
	}

}
