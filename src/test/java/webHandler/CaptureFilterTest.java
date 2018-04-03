package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class CaptureFilterTest {

    private Capture capture;
    private LogFilter logFilter;
    private String logData;
    private Date startTime;
    private Date endTime;
    private int transactionLimit;


    @Before
    public void before()
    {
        logData = "/rdsdbbin/mysql/bin/mysqld, Version: 5.6.37-log (MySQL Community Server (GPL)). started with:\n" +
                "Tcp port: 3306 Unix socket: /tmp/mysql.sock\n" +
                "Time Id Command Argument\n" +
                "180217 21:08:52  2    Connect    rdsadmin@ip\n" +
                "180217 21:08:52	6968 Connect	admin@ip on testdb\n" +
                "180217 21:08:53	6968 Query	/* mysql-connector-java-5.1.44 SELECT " +
                "@@session.auto_increment_increment\n" +
                "6968 Query	SET NAMES latin1\n" +
                "6968 Query	SET character_set_results = NULL\n" +
                "6968 Query	SET autocommit=1\n" +
                "6968 Query	SET sql_mode='NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES'\n" +
                "6968 Query	select @@session.tx_read_only\n" +
                "180217 21:08:57	2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from information_schema.TABLES WHERE TABLE_SCHEMA = " +
                "'mysql' AND TABLE_NAME = 'rds_heartbeat2'\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT value FROM mysql.rds_heartbeat2\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from information_schema.TABLES WHERE TABLE_SCHEMA = " +
                "'mysql' AND TABLE_NAME = 'rds_heartbeat2'\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	select @@session.tx_read_only\n" +
                "2 Query	INSERT INTO mysql.rds_heartbeat2(id, value) values (1,0) ON DUPLICATE KEY UPDATE value = 0\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	select @@session.tx_read_only\n" +
                "2 Query	COMMIT\n" +
                "180217 21:09:00	2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from mysql.rds_history WHERE action = 'disable set master' " +
                "GROUP BY action_timestamp,called_by_user,action,mysql_version,master_host,master_port," +
                "master_user,master_log_file ,master_log_pos,master_ssl ORDER BY action_timestamp LIMIT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from mysql.rds_replication_status WHERE master_host IS NOT NULL " +
                "and master_port IS NOT NULL GROUP BY action_timestamp,called_by_user,action,mysql_version," +
                "master_host,master_port ORDER BY action_timestamp LIMIT 1\n" +
                "180217 21:09:11	6969 Connect	admin@ip on testdb\n" +
                "6969 Query	/* mysql-connector-java-5.1.44  */SELECT " +
                "@@session.auto_increment_increment AS auto_increment_increment,\n" +
                "6969 Query	SET NAMES latin1\n" + "6969 Query	SET character_set_results = NULL\n" +
                "6969 Query	SET autocommit=1\n" +
                "6969 Query	SET sql_mode='NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES'\n" +
                "6969 Query	select @@session.tx_read_only\n" +
                "6969 Query	CREATE TABLE Users (value INT PRIMARY KEY)\n" +
                "180217 21:09:12	2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from information_schema.TABLES WHERE TABLE_SCHEMA = " +
                "'mysql' AND TABLE_NAME = 'rds_heartbeat2'\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT value FROM mysql.rds_heartbeat2\n" +
                "180217 21:09:27	2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from information_schema.TABLES WHERE TABLE_SCHEMA = " +
                "'mysql' AND TABLE_NAME = 'rds_heartbeat2'\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT value FROM mysql.rds_heartbeat2\n" +
                "180217 21:09:42	2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT count(*) from information_schema.TABLES WHERE TABLE_SCHEMA = " +
                "'mysql' AND TABLE_NAME = 'rds_heartbeat2'\n" +
                "2 Query	SELECT 1\n" +
                "2 Query	SELECT value FROM mysql.rds_heartbeat2\n";

        startTime = new Date(5);
        endTime = new Date();
        transactionLimit = 0;
        capture = new Capture("id", "", "", startTime, endTime, 0, transactionLimit);
    }

    @Test
    public void noDataLogFilter()
    {
        List<String> statementsToRemove = new ArrayList<>();
        List<String> usersToRemove = new ArrayList<>();

        usersToRemove.add("admin");

        capture.setFilterStatements(statementsToRemove);
        capture.setFilterUsers(usersToRemove);

        logFilter = new CaptureFilter(capture);
        List<Statement> filteredStatementList = logFilter.filterLogData(logData);

        List<String> noDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());

        String noData = String.join(",\n", noDataList);
        assertEquals("", noData);
    }

    @Test
    public void userDataLogFilter()
    {
        List<String> statementsToRemove = new ArrayList<>();
        List<String> usersToRemove = new ArrayList<>();

        String expected = "{\n" +
                "\"date\": \"180217\",\n" +
                "\"time\": \"21:08:52\",\n" +
                "\"id\": 6968,\n" +
                "\"command\": \"Connect\",\n" +
                "\"query\": \"admin@ip on testdb\"\n" +
                "},\n" +
                "{\n" +
                "\"date\": \"180217\",\n" +
                "\"time\": \"21:09:11\",\n" +
                "\"id\": 6969,\n" +
                "\"command\": \"Connect\",\n" +
                "\"query\": \"admin@ip on testdb\"\n" +
                "},\n" +
                "{\n" +
                "\"date\": \"180217\",\n" +
                "\"time\": \"21:09:11\",\n" +
                "\"id\": 6969,\n" +
                "\"command\": \"Query\",\n" +
                "\"query\": \"CREATE TABLE Users (value INT PRIMARY KEY)\"\n" +
                "}";

        logFilter = new CaptureFilter(capture);

        List<Statement> filteredStatementList = logFilter.filterLogData(logData);

        List<String> userDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());

        String userData = String.join(",\n", userDataList);
        assertEquals(expected, userData);
    }


    @After
    public void after()
    {
        logFilter = null;
        assertNull(logFilter);
    }
}
