package webHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ReplayFilterTest {

    private LogFilter logFilter;
    private String logData;


    @Before
    public void before()
    {
        logData = "[\n{\n" +
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
                "}\n]";

    }

    @Test
    public void noDataLogFilter()
    {
        List<String> statementsToRemove = new ArrayList<>();
        List<String> usersToRemove = new ArrayList<>();
        Replay replay = new Replay();


        usersToRemove.add("admin");
        replay.setFilterStatements(statementsToRemove);
        replay.setFilterUsers(usersToRemove);

        logFilter = new ReplayFilter(replay);

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

        Replay replay = new Replay();
        replay.setFilterStatements(statementsToRemove);
        replay.setFilterUsers(usersToRemove);
        logFilter = new ReplayFilter(replay);

        List<Statement> filteredStatementList = logFilter.filterLogData(logData);
        List<String> userDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());

        String userData = String.join(",\n", userDataList);

        // expected is the same as logData except the leading '[' and trailing ']'
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

        assertEquals(expected, userData);
    }

    @After
    public void after()
    {
        logFilter = null;
        assertNull(logFilter);
    }
}
