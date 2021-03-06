package net.ion.external.ics.common;

import java.io.IOException;
import java.util.concurrent.Executor;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.handler.authentication.PasswordAuthenticator;

public class MyVerifier implements PasswordAuthenticator {

	private ReadSession session;

	private MyVerifier(ReadSession session) {
		this.session = session;
	}

	public static MyVerifier test(ReadSession session) throws IOException {
		return new MyVerifier(session).addUser("admin", "admin", "success");
	}

	// Only Use Test
	MyVerifier addUser(final String userId, final String name, final String password) throws IOException {
		try {
			session.tranSync(new TransactionJob<Void>() {
				@Override
				public Void handle(WriteSession wsession) throws Exception {
					if (!wsession.exists("/users/" + userId)) {
						wsession.pathBy("/users/" + userId).property(Def.User.Name, name).encrypt(Def.User.Password, password);
					}
					return null;
				}
			});
		} catch (Exception ex) {
			throw new IOException(ex);
		}
		session.workspace().wsName();

		return this;
	}

	public void authenticate(HttpRequest request, String username, String password, ResultCallback callback, Executor handlerExecutor) {
		ReadNode found = session.ghostBy("/users/" + username);
		String expectedPassword = found.property(Def.User.Password).stringValue();
		try {
			if (expectedPassword != null && found.isMatch(Def.User.Password, password)) {
				String langcode = found.property(MyAuthenticationHandler.LANGCODE).defaultValue("us");
				request.data(MyAuthenticationHandler.LANGCODE, langcode);

				callback.success();
			} else {
				callback.failure();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			callback.failure(); 
		}
	}
}
