package webHandler;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReplayFilter extends LogFilter {

    public ReplayFilter(List<String> statementsToRemove, List<String> usersToRemove)
    {
        this.statementsToRemove = statementsToRemove;
        this.usersToRemove = usersToRemove;
        this.transactionLimit = 0; // currently replay does not have a transactionLimit
    }

    private List<Statement> parseFromJSON(String logData)
    {
        List<Statement> logStatements = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try
        {
            Object objArray = parser.parse(logData);

            // cast the obj to a JSONArray
            JSONArray logArray = (JSONArray) objArray;
            // loop through array and create a statement for every one
            for (Object obj : logArray)
            {
                JSONObject statementObject = (JSONObject) obj;
                String date = (String) statementObject.get("date");
                String time = (String) statementObject.get("time");
                int id = ((Long) statementObject.get("id")).intValue();
                String command = (String) statementObject.get("command");
                String query = (String) statementObject.get("query");

                Statement statement = new Statement(date, time, id, command, query);
                logStatements.add(statement);
            }
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }
        return logStatements;

    }

    public List<Statement> filterLogData(String logData)
    {
        // resulting filtered array of statements
        List<Statement> filteredLogStatements = new ArrayList<>();
        // logData represented as an array of statements
        List<Statement> logStatements = parseFromJSON(logData);
        // add all the default filtered values
        addDefaultFilterValues();

        for (Statement statement : logStatements)
        {
            // Filters the user-selected users and statements
            if (isUserSelectedUser(statement) ||
                    idToRemove.contains(statement.getId()) ||
                    isUserSelectedStatement(statement))
            {
                continue;
            }
            // if the statement is not a connection, add 1 to current transaction count
            if (!(isConnectCommand(statement.getCommand())))
            {
                transactionCount++;
            }

            // if the statement passed all filters then add
            filteredLogStatements.add(statement);

        }

        return filteredLogStatements;
    }


}