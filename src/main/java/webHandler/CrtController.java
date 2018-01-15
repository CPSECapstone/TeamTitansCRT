package webHandler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CrtController {

    @GetMapping
    public String index() { return "indexPage.html"; }

    @GetMapping("settings")
    public String settings() { return "settingsPage.html"; }

    @GetMapping("analyze")
    public String analyze() { return "analyzePage.html"; }
    
    @GetMapping("login")
    public String login() { return "setupPage.html"; }

    @GetMapping("dashboard")
    public String dashboard() { return "dashboardPage.html"; }
}
