package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class CrtControllerTest {
    @Test
    public void index() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("settingsPage.html", crtCon.index());
    }

    @Test
    public void main() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("index.html", crtCon.main());
    }

}