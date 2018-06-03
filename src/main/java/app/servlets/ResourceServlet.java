package app.servlets;

import app.managers.RDSManager;
import com.amazonaws.regions.Regions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import app.util.DBUtil;
import app.models.Capture;
import app.models.Replay;
import app.managers.S3Manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Servlet to handle all resource calls.
 */
@RestController
public class ResourceServlet {

    public static ArrayList<Regions> rdsRegions = null;

    public static ArrayList<Regions> s3Regions = null;

    public static ArrayList<Regions> getRdsRegions() {
        ArrayList<Regions> allRegions = new ArrayList<>();
        Collections.addAll(allRegions, Regions.values());
        allRegions.remove(Regions.valueOf("GovCloud"));
        allRegions.remove(Regions.valueOf("CN_NORTH_1"));

        ArrayList<Regions> populatedRegions = new ArrayList<>();

        for(Regions region : allRegions) {
            if (!new RDSManager(region.name()).getRDSInstances().isEmpty()) {
                populatedRegions.add(region);
            }
        }

        return populatedRegions;
    }

    public static ArrayList<Regions> getS3Regions() {
        ArrayList<Regions> allRegions = new ArrayList<>();
        Collections.addAll(allRegions, Regions.values());
        allRegions.remove(Regions.valueOf("GovCloud"));
        allRegions.remove(Regions.valueOf("CN_NORTH_1"));

        ArrayList<Regions> populatedRegions = new ArrayList<>();

        for(Regions region : allRegions) {
            if (!new S3Manager(region.name()).getS3Buckets().isEmpty()) {
                populatedRegions.add(region);
            }
        }

        return populatedRegions;
    }

    @RequestMapping(value = "/resource/rds/{region}", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getRDSInstances(@PathVariable String region) {

        RDSManager rdsManager = new RDSManager(region);
        return new ResponseEntity<>(rdsManager.getRDSInstances(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/s3/{region}", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getS3Buckets(@PathVariable String region) {
        S3Manager s3Manager = new S3Manager(region);
        return new ResponseEntity<>(s3Manager.getS3Buckets(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/regions", method = RequestMethod.GET)
    public ResponseEntity<Regions[]> getRegions() {
        ArrayList<Regions> regions = new ArrayList<>();
        Collections.addAll(regions, Regions.values());
        regions.remove(Regions.valueOf("GovCloud"));
        regions.remove(Regions.valueOf("CN_NORTH_1"));

        return new ResponseEntity<>(regions.toArray(new Regions[regions.size()]), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/regions/rds", method = RequestMethod.GET)
    public ResponseEntity<Regions[]> getRegionsWithRDSInstances() {
        if (rdsRegions == null) {
            rdsRegions = getS3Regions();
        }

        return new ResponseEntity<>(rdsRegions.toArray(new Regions[rdsRegions.size()]), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/regions/s3", method = RequestMethod.GET)
    public ResponseEntity<Regions[]> getRegionsWithS3Instances() {
        if (s3Regions == null) {
            s3Regions = getS3Regions();
        }

        return new ResponseEntity<>(s3Regions.toArray(new Regions[s3Regions.size()]), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/captures", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> getAllCaptureHistory() {

        ArrayList<Capture> captures = DBUtil.getInstance().loadAllCaptures();

        if (captures == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(captures, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/captures/{status}", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> getCapturesWithStatus(@PathVariable String status) {
        ArrayList<Capture> captures = DBUtil.getInstance().loadCapturesWithStatus(status);

        if (captures == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(captures, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/replays", method = RequestMethod.GET)
    public ResponseEntity<Collection<Replay>> getAllReplayHistory() {

        ArrayList<Replay> replays = DBUtil.getInstance().loadAllReplays();

        if (replays == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/replays/{status}", method = RequestMethod.GET)
    public ResponseEntity<Collection<Replay>> getReplaysWithStatus(@PathVariable String status) {
        ArrayList<Replay> replays = DBUtil.getInstance().loadReplaysWithStatus(status);

        if (replays == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(replays, HttpStatus.OK);
    }
}
