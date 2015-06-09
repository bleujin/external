package net.ion.airkjh;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.OracleDBManager;
import net.ion.framework.db.procedure.IParameterQueryable;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.StringUtil;

public class TestDumb extends TestCase {

    private Craken r;
    private ReadSession session;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.r = Craken.inmemoryCreateWithTest();
        this.session = r.login("test");
    }

    @Override
    protected void tearDown() throws Exception {
        r.shutdown();
        super.tearDown();
    }

    public void testParam() throws Exception {
        OracleDBManager dbm = new OracleDBManager("jdbc:oracle:thin:@dev-oracle.i-on.net:1521:dev10g", "dev_ics6", "dev_ics6");
        DBController dc = new DBController(dbm);

        IParameterQueryable command = dc.createUserCommand("select 'hello '||:user greeting from dual").addParam("user", "airkjh");
        Rows rs = command.execQuery();
        rs.next();
        Debug.line(rs.getString("greeting"));

        dc.close();
    }

    public void testWriteSessionClear() throws IOException {
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
    }

    public void testSort() throws IOException {
        // given
        session.tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                wsession.pathBy("/airkjh/1").property("num",1);
                wsession.pathBy("/airkjh/2").property("num",2);
                wsession.pathBy("/airkjh/3").property("num",3);
                wsession.pathBy("/airkjh/4").property("num",4);
                return null;
            }
        });

        // when
        IteratorList<ReadNode> iterator = session.pathBy("/airkjh").childQuery("").descendingNum("num").find().iterator();

        // then
        while(iterator.hasNext()) {
            Debug.line(iterator.next().property("num")) ;
        }
    }


    public void testSubstrBetween() {
        String src = "abc";
        String[] results = StringUtil.substringsBetween(src, "a", "d");

        assertEquals(null, results);
    }

    public void testRE() {
        Pattern p = Pattern.compile("^(resize|crop)[0-9]+\\_[0-9]+$");
        assertTrue(p.matcher("resize137_137").matches());
    }

}
