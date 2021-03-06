package unittests.managers;

import app.managers.S3Manager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class S3ManagerTest {

    private S3Manager s3Manager;

    @Before
    public void before() throws Exception {
        File f = new File(".privateKeys");
        org.junit.Assume.assumeTrue(f.exists() && f.isFile());
        s3Manager = new S3Manager("US_WEST_1");
    }

    @Test
    public void uploadFile() throws Exception {
        File file = new File("test-Workload.log");
        org.junit.Assume.assumeTrue(file.exists() && file.isFile());
        InputStream inputStream = new FileInputStream(file);
        s3Manager.uploadFile("teamtitans-test-mycrt", "test-Workload.log", inputStream, null);
        inputStream.close();

        InputStream retrievedStream = s3Manager.getFile("teamtitans-test-mycrt", "test-Workload.log");
        retrievedStream.close();
        assertNotNull(retrievedStream);
    }

//    @Test
//    public void getFile() throws Exception {
//        InputStream inputStream = s3Manager.getFile("teamtitans-test-mycrt", "test-Workload.log");
//        inputStream.close();
//        assertNotNull(inputStream);
//    }

//    @Test
//    public void downloadFileLocally() throws IOException {
//        s3Manager.downloadFileLocally("teamtitans-test-mycrt", "test-Workload.log", "test-file.log");
//        File f = new File("test-file.log");
//        boolean foundFile = f.exists() && !f.isDirectory();
//        f.delete();
//        assertTrue(foundFile);
//    }

    @Test
    public void deleteFile() throws Exception {
        File file = new File("test-Workload.log");
        org.junit.Assume.assumeTrue(file.exists() && file.isFile());
        InputStream inputStream = new FileInputStream(file);
        s3Manager.uploadFile("teamtitans-test-mycrt", "test-Workload.log", inputStream, null);
        inputStream.close();

        InputStream retrievedStream = s3Manager.getFile("teamtitans-test-mycrt", "test-Workload.log");
        retrievedStream.close();
        assertNotNull(retrievedStream);

        s3Manager.deleteFile("teamtitans-test-mycrt", "test-Workload.log");
        retrievedStream = s3Manager.getFile("teamtitans-test-mycrt", "test-Workload.log");
        retrievedStream.close();
        assertNull(retrievedStream);
    }

    @After
    public void after() throws Exception {
        s3Manager = null;
        assertNull(s3Manager);
    }
}