package webHandler;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
public class AuthController {

    @RequestMapping(value = "/auth/{key}", method = RequestMethod.POST)
    public ResponseEntity<String> Authenticate(@PathVariable("key") String key) {
        return new ResponseEntity<String>(key, HttpStatus.OK);
    }

}
