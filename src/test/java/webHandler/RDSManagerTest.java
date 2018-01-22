package webHandler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;
import static org.junit.Assert.*;

public class RDSManagerTest {

    @Before
    public void beforeMethod() {
        File f = new File(".privateKeys");
        org.junit.Assume.assumeTrue(f.exists() && f.isFile());
    }

    @Test
    public void downloadLog() throws Exception {
        RDSManager rdsManager = new RDSManager();

        InputStream actualInput = rdsManager.downloadLog("testdb", "general/mysql-general.log");
        Scanner s = new Scanner(actualInput).useDelimiter("\\A");
        String actual = s.hasNext() ? s.next() : "";
        actualInput.close();
        s.close();

        InputStream expectedInput = new FileInputStream("mysql-general.txt");
        s = new Scanner(expectedInput).useDelimiter("\\A");
        String expected = s.hasNext() ? s.next() : "";
        expectedInput.close();
        s.close();

        //The actual log always appends new changes so everything won't be the same.
        assertEquals(expected.substring(0,100), actual.substring(0,100));
    }

}