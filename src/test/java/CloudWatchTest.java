import com.amazonaws.SdkClientException;
import org.junit.Test;
import webHandler.CloudWatchManager;

import static org.junit.Assert.*;

public class CloudWatchTest {

    @Test
    public void testCloudWatch() throws SdkClientException {
        assertNotNull(new CloudWatchManager());
    }
}