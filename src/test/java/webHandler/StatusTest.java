package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatusTest {

    private StatusMetrics statusMetrics;
    private Status status;

    @Before
    public void before() throws Exception {
        statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        status = new Status("online", statusMetrics);
    }

    @Test
    public void getStatus() throws Exception {
        assertEquals("online", status.getStatus());
    }

    @Test
    public void getMetrics() throws Exception {
        assertNotNull(status.getMetrics());
    }

    @After
    public void after() throws Exception {
        statusMetrics = null;
        status = null;
        assertNull(status);
    }
}