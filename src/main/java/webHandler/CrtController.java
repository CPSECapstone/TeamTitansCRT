package webHandler;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class CrtController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to MyCRT!";
    }

}
