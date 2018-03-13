package webHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity<String> Authenticate(@RequestBody String key) {
        return new ResponseEntity<String>("IAM Key: " + key, HttpStatus.OK);
    }

}
