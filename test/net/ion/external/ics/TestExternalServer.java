package net.ion.external.ics;

import junit.framework.TestCase;
import net.ion.framework.util.InfinityThread;

public class TestExternalServer extends TestCase {


    public void testRun() throws Exception {
        final ExternalServer server = ExternalServer.create(9001).start();


        new InfinityThread().startNJoin();
    }
}
