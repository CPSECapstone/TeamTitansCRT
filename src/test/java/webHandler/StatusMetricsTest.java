package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class StatusMetricsTest {
    @Test
    public void getCpu() throws Exception {
        StatusMetrics statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        assertEquals(1.3, statusMetrics.getCpu(),0.0);
    }

    @Test
    public void getRam() throws Exception {
        StatusMetrics statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        assertEquals(2.2, statusMetrics.getRam(),0.0);
    }

    @Test
    public void getDisk() throws Exception {
        StatusMetrics statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        assertEquals(3.1, statusMetrics.getDisk(),0.0);
    }
}