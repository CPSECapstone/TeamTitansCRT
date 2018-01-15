package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthControllerTest {
    @Test
    public void authenticate() throws Exception {
        AuthController auth = new AuthController();
        String actual = auth.Authenticate("key").getBody();
        assertEquals("IAM Key: key", actual);
    }

}