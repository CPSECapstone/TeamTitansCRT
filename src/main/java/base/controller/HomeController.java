
package base.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller// so framework can recognize this as a controller class
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index() { return "index.html"; }

    @GetMapping("main")
    public String main() { return "mainPage.html"; }

    @GetMapping("sign-in")
    public String signIn() { return "signIn.html"; }

    @GetMapping("sign-up")
    public String signUp() { return "signUp.html"; }

    @GetMapping("sign-out")
    public String signOut() { return "signOut.html"; }
}
