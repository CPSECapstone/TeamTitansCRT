package unittests.controllers;

import webhandler.AuthController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthControllerTest {
    @Test
    public void authenticate() throws Exception {
        AuthController auth = new AuthController();
        String actual = auth.Authenticate("key").getBody();
        assertEquals("IAM Key: key", actual);
    }

}