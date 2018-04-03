package webHandler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CrtController {

    @GetMapping
    public String index() { return "my-setup.html"; }

    @GetMapping("settings") 
    public String settings() { return "my-settings.html"; }

    @GetMapping("capture")
    public String capture() { return "my-captures.html"; }

    @GetMapping("replay")
    public String replay() { return "my-replays.html"; }

    @GetMapping("analyze")
    public String analyze() { return "analyzePage.html"; }

    @GetMapping("login")
    public String login() { return "my-setup.html"; }

    @GetMapping("dashboard")
    public String dashboard() { return "my-dashboard.html"; }
}
