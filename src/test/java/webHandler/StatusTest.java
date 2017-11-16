package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class StatusTest {
    @Test
    public void getStatus() throws Exception {
        StatusMetrics statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        Status status = new Status("online", statusMetrics);
        assertEquals("online", status.getStatus());
    }

    @Test
    public void getMetrics() throws Exception {
        StatusMetrics statusMetrics = new StatusMetrics(1.3, 2.2, 3.1);
        Status status = new Status("online", statusMetrics);
        assertNotNull(status.getMetrics());
    }

}