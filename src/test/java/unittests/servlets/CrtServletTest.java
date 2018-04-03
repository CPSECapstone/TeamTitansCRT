package unittests.servlets;

import app.servlets.CrtServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CrtServletTest {

    private CrtServlet crtCon;

    @Before
    public void before() throws Exception {
        crtCon = new CrtServlet();
    }

    @Test
    public void index() throws Exception {
        assertEquals("my-setup.html", crtCon.index());
    }

    @Test
    public void settings() throws Exception {
        assertEquals("my-settings.html", crtCon.settings());
    }

    @Test
    public void analyze() throws Exception {
        assertEquals("analyzePage.html", crtCon.analyze());
    }
    
    @Test
    public void login() throws Exception {
        assertEquals("my-setup.html", crtCon.login());
    }
    
    @Test
    public void dashboard() throws Exception {
        assertEquals("my-dashboard.html", crtCon.dashboard());
    }

    @After
    public void after() throws Exception {
        crtCon = null;
        assertNull(crtCon);
    }
}
