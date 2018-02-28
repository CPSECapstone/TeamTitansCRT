package webHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * Servlet to handle all resource calls.
 */
@RestController
public class ResourceServlet {

    @RequestMapping(value = "/resource/rds", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getRDSInstances() {

        RDSManager rdsManager = new RDSManager();
        return new ResponseEntity<>(rdsManager.getRDSInstances(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/s3", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getS3Buckets() {

        S3Manager s3Manager = new S3Manager();
        return new ResponseEntity<>(s3Manager.getS3Buckets(), HttpStatus.OK);
    }
}
