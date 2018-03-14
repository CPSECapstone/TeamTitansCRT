package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StatusMetricsTest {

    private StatusMetrics statusMetrics;

    @Before
    public void before() throws Exception {
        statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
    }

    @Test
    public void getCpu() throws Exception {
        assertEquals(1.3, statusMetrics.getCpu(),0.0);
    }

    @Test
    public void getRam() throws Exception {
        assertEquals(2.2, statusMetrics.getRam(),0.0);
    }

    @Test
    public void getDisk() throws Exception {
        assertEquals(3.1, statusMetrics.getDisk(),0.0);
    }

    @After
    public void after() throws Exception {
        statusMetrics = null;
        assertNull(statusMetrics);
    }
}