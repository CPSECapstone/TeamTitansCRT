package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import static org.junit.Assert.*;

public class RDSManagerTest {

    private RDSManager rdsManager;

    @Before
    public void before() throws Exception {
        File f = new File(".privateKeys");
        org.junit.Assume.assumeTrue(f.exists() && f.isFile());
        rdsManager = new RDSManager();
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