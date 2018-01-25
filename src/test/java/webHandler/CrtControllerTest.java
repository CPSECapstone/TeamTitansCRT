package webHandler;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class CrtControllerTest {

    private CrtController crtCon;

    @Before
    public void before() throws Exception {
        crtCon = new CrtController();
    }

    @Test
    public void index() throws Exception {
        assertEquals("setupPage.html", crtCon.index());
    }

    @Test
    public void settings() throws Exception {
        assertEquals("settingsPage.html", crtCon.settings());
    }

    @Test
    public void analyze() throws Exception {
        assertEquals("analyzePage.html", crtCon.analyze());
    }
    
    @Test
    public void login() throws Exception {
        assertEquals("setupPage.html", crtCon.login());
    }
    
    @Test
    public void dashboard() throws Exception {
        assertEquals("dashboardPage.html", crtCon.dashboard());
    }

    @After
    public void after() throws Exception {
        crtCon = null;
        assertNull(crtCon);
    }
}
