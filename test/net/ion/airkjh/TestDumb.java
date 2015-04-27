package net.ion.airkjh;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.RepositoryImpl;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.db.procedure.IParameterQueryable;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestDumb extends TestCase {

    private RepositoryImpl r;
    private ReadSession session;

    public void testParam() throws Exception {
		OracleDBManager dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
		DBController dc = new DBController(dbm) ;
		
		IParameterQueryable command = dc.createUserCommand("select 'hello '||:user greeting from dual").addParam("user", "airkjh") ;
		Rows rs = command.execQuery();
		rs.next() ;
		Debug.line(rs.getString("greeting")) ;
		
		dc.close();
	}

    public void testWriteSessionClear() throws IOException {
        RepositoryImpl r = RepositoryImpl.inmemoryCreateWithTest();
        ReadSession session = r.login("test");
        try {
            final FileInputStream in = new FileInputStream("./resource/log4j.properties");

            session.tran(new TransactionJob<Void>() {
                @Override
                public Void handle(WriteSession wsession) throws Exception {
                    wsession.pathBy("/airkjh").property("id", "airkjh").blob("bbb", in);
                    return null;
                }
            });

            IOUtil.closeQuietly(in);

            ReadNode node = session.pathBy("/airkjh");
            Debug.line(node.property("bbb").asBlob().toString());

            session.tran(new TransactionJob<Void>() {
                @Override
                public Void handle(WriteSession wsession) throws Exception {
                    wsession.pathBy("/airkjh").clear();
                    return null;
                }
            });

            assertEquals(false, node.isGhost());
            assertEquals(false, node.hasProperty("id"));
            assertEquals(false, node.hasProperty("bbb"));
            node.debugPrint();
        } finally {
            r.shutdown();
        }
    }

    public void testSubstrBetween() {
        String src = "abc" ;
        String[] results = StringUtil.substringsBetween(src, "a", "d");

        assertEquals(null, results) ;
    }
}
