package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class CrtControllerTest {
    @Test
    public void index() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("Welcome to MyCRT!", crtCon.index());
    }

}