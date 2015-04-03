package net.ion.bleujin;

import java.util.Map;

import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.external.ICSSubCraken;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;

public class TestCDD extends TestCase {

	public void testFirst() throws Exception {
		ICSSubCraken ics = ICSSubCraken.test() ;
		final ReadSession session = ics.login() ;
		
		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/dept/{deptid}";
			}
			
			@Override
			public TransactionJob<Void> modified(Map<String, String> map, CDDModifiedEvent cddmodifiedevent) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public TransactionJob<Void> deleted(Map<String, String> map, CDDRemovedEvent cddremovedevent) {
				final String deptId = map.get("deptid") ;
				Debug.line(deptId, session.ghostBy("/dept/" + deptId));
				session.ghostBy("/dept/" + deptId).children().debugPrint(); 
				return null;
			}
		}) ;
		
		
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/dept/dev/bleujin").property("name", "bleujin") ;
				wsession.pathBy("/dept/dev/hero").property("name", "hero") ;
				wsession.pathBy("/dept/dev/jin").property("name", "jin") ;
				return null;
			}
		}) ;
		
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/dept").removeSelf() ;
				return null;
			}
		}) ;
		
	}
}
