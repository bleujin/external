package net.ion.external.ics;

import junit.framework.TestCase;
import net.ion.framework.util.InfinityThread;

public class TestICSServer extends TestCase {


    public void testRun() throws Exception {
        final ICSServer server = ICSServer.create(9001).start();


        new InfinityThread().startNJoin();
    }
}
