package net.ion.bleujin;

import net.ion.craken.Craken;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import junit.framework.TestCase;

public class TestRefSearch extends TestCase {

	public void testFirst() throws Exception {
		Craken craken = Craken.test() ;
		ReadSession session = craken.login() ;
		
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("dept", "/dev").refTo("company", "ion") ;
				return null;
			}
		}) ;
		
		session.root().childQuery("@dept:\"/dev\"", true).find().debugPrint();
	}
}
