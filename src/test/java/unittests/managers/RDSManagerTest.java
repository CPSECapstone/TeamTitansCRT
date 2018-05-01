package unittests.managers;

import app.managers.RDSManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RDSManagerTest {

    private RDSManager rdsManager;

    @Before
    public void before() throws Exception {
        File f = new File(".privateKeys");
        org.junit.Assume.assumeTrue(f.exists() && f.isFile());
        rdsManager = new RDSManager("US_WEST_1");
    }

    @Test
    public void downloadLog() throws Exception {
        String logData = rdsManager.downloadLog("testdb", "general/mysql-general.log");
        assertNotNull(logData);
    }

    @After
    public void after() throws Exception {
        rdsManager = null;
        assertNull(rdsManager);
    }

}