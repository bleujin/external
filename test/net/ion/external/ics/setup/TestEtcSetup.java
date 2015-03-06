package net.ion.external.ics.setup;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ion.craken.node.IteratorList;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.util.Debug;

public class TestEtcSetup extends TestSetup{

	public void testAfield() throws Exception {
		String procSQL = "select '/afield/' || afieldid fqn, afieldid, afieldnm name, afieldexp explain, typeCd, defaultValue " +
						" /* grpCd groupCd, afieldLen length, afieldVlen, vertialLength, isMndt isMandatory, index_option indexOption, fileTypeCd , examId */" +
						" from afield_tblc"	;
		
		Debug.line(saveToCraken(procSQL) + " applied");
	}
	

	public void testRelation() throws Exception {
		String procSQL = "select  '/afield/' || upperId fqn, lowerId, orderno from afield_rel_tblc " +
						 " where upperId != 'ROOT' order by upperId, orderno" ;
		
		dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).refTos("include", rs.getString("lowerid")) ;
						}
						return null;
					}
				}) ;
				return null;
			}
		}) ;
	}
	
	public void testViewAfield() throws Exception {
		session.pathBy("/afield").walkChildren().debugPrint(); 
	}

	

	public void testUser() throws Exception {
		super.resetChildren("/user");
		
		String procSQL = "select '/user/' || userId fqn, userid, usernm name, userid password, isUser from user_tblc where retireDay> to_char(sysdate, 'yyyymmdd')" ;
		Debug.line(saveToCraken(procSQL) + " applied");

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				IteratorList<WriteNode> iter = wsession.pathBy("/user").children().iterator() ;
				while(iter.hasNext()){
					WriteNode wnode = iter.next() ;
					wnode.encrypt("password", wnode.property("password").asString()) ;
				}
				return null;
			}
		}) ;
	}
	
	public void testUserRelation() throws Exception {
		String procSQL = "select '/user/' || upperUserId fqn, lowerUserId lowerid from user_rel_tblc" ;

		dc.createUserCommand(procSQL).execHandlerQuery(new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(final ResultSet rs) throws SQLException {
				session.tran(new TransactionJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						while(rs.next()){
							wsession.pathBy(rs.getString("fqn")).refTos("include", rs.getString("lowerid")) ;
						}
						return null;
					}
				}) ;
				return null;
			}
		}) ;
	}
	
	
	public void testView() throws Exception {
		session.pathBy("/user").walkChildren().debugPrint();
	}
	
	
	
}
