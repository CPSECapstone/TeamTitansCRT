package webHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class CrtControllerTest {
    @Test
    public void index() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("indexPage.html", crtCon.index());
    }

    @Test
    public void settings() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("settingsPage.html", crtCon.settings());
    }

    @Test
    public void analyze() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("analyzePage.html", crtCon.analyze());
    }
    
    @Test
    public void login() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("setupPage.html", crtCon.login());
    }
    
    @Test
    public void dashboard() throws Exception {
        CrtController crtCon = new CrtController();
        assertEquals("dashboardPage.html", crtCon.dashboard());
    }
}
