package webHandler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CrtController {

    @GetMapping
    public String index() { return "settingsPage.html"; }

    @GetMapping("main")
    public String main() { return "index.html"; }

}
