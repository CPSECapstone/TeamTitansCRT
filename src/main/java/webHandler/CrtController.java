package webHandler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CrtController {

    @GetMapping
    public String index() { return "setupPage.html"; }

    @GetMapping("settings")
    public String settings() { return "settingsPage.html"; }

    @GetMapping("capture")
    public String capture() { return "startCapturePage.html"; }
    
    @GetMapping("manageCaptures")
    public String manageCaptures() { return "manageCapturePage.html"; }

    @GetMapping("replay")
    public String replay() { return "startReplayPage.html"; }
    
    @GetMapping("manageReplays")
    public String manageReplays() { return "manageReplayPage.html"; }

    @GetMapping("analyze")
    public String analyze() { return "analyzePage.html"; }

    @GetMapping("login")
    public String login() { return "setupPage.html"; }

    @GetMapping("dashboard")
    public String dashboard() { return "dashboardPage.html"; }
}
