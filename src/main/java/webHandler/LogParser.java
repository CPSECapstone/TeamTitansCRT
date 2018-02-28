package webHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LogParser {

    private class Statement
    {
        public String date;
        public String time;
        public int id;
        public String command;
        public String query;

        public Statement(String date, String time, int id, String command, String query)
        {
            this.date = date;
            this.time = time;
            this.id = id;
            this.command = command;
            this.query = query;
        }

        public String toString()
        {
            return "{\n" + "\"date\": \"" + this.date + "\",\n\"time\": \"" + this.time +
                    "\",\n\"id\": " + this.id + ",\n\"command\": \"" + this.command +
                    "\",\n\"query\": \"" + this.query + "\"\n}";
        }


    }

    private String previousTime;
    private String previousDate;
    private List<Integer> idToRemove = new ArrayList<>();

    public LogParser()
    {
        this.previousTime = "";
        this.previousDate = "";
    }

    public LogParser(String previousTime, String previousDate)
    {
        this.previousDate = previousDate;
        this.previousTime = previousTime;
    }

    private boolean filterHeader(String line)
    {
        return line.matches("(?i).*tcp port.*") ||
                line.matches("(?i).*rdsdbbin.*") ||
                line.matches("(?i)Time.*Id.*Command.*Argument");
    }

    private boolean filterUserSelectedUsers(Statement statement, List<String> usersToRemove)
    {
        // add the default rdsadmin
        usersToRemove.add("rdsadmin");
        // Match any users connections with user-selected users
        if (statement.command.equals("Connect") && usersToRemove.contains(statement.query.split("@")[0]))
        {
            idToRemove.add(statement.id);
            return true;
        }

        return false;
    }

    private boolean filterUserSelectedStatements(Statement statement, List<String> statementsToRemove)
    {
        statementsToRemove.add("Statistics");
        statementsToRemove.add("Quit");
        for (String statementToRemove : statementsToRemove)
        {
            // Regex match any user-selected statements with current queries
            if (statement.query.matches("(?i)" + statementToRemove + ".*"))
            {
                return true;
            }
        }
        return false;
    }

    private boolean filterRDSDefaultStatements(Statement statement)
    {
        // don't think this is an exhaustive list... might not even need

        if (statement.query.equals("SELECT 1") ||
                statement.query.equals("Statistics") ||
                statement.query.equals("COMMIT") ||
                statement.query.matches("(?i).*rds_.*") ||
                statement.query.matches("(?i).*purge binary logs to.*") ||
                statement.query.matches("(?i).*@@session.*") ||
                statement.query.matches("(?i)show global variables like.*") ||
                statement.query.matches("(?i)flush logs.*") ||
                statement.query.matches("(?i)set autocommit.*") ||
                statement.query.matches("(?i)set sql.*") ||
                statement.query.matches("(?i)set character_set_results.*") ||
                statement.query.matches("(?i)set names.*") ||
                statement.query.matches("(?i)Show engines") ||
                statement.query.matches("(?i)show session.*") ||
                statement.query.matches("(?i)show function status") ||
                statement.query.matches("(?i)show full tables.*"))
        {
            return true;
        }
        return false;
    }

    private String adhereToTimeLayout(String time)
    {
        String[] timeArray = time.split(":");
        for (int i = 0; i < timeArray.length; i++)
        {
            if (timeArray[i].trim().length() <= 1)
            {
                timeArray[i] = "0" + timeArray[i];
            }
        }
        return String.join(":", timeArray);
    }

    private Statement createStatement(String stmt)
    {
        List<String> currentStatement = new ArrayList<>(Arrays.asList(stmt.split("\\s")));
        currentStatement.removeAll(Arrays.asList("", null));

        if (currentStatement.get(1).split(":").length >= 3)
        {
            previousDate = currentStatement.get(0);
            previousTime = currentStatement.get(1);
        }
        else if (previousTime.equals("") || previousDate.equals(""))
        {
            // currently if no time or date is available then use this format
            currentStatement.add(0, "--:--:--");
            currentStatement.add(0, "------");
        }
        else
        {
            currentStatement.add(0, previousTime);
            currentStatement.add(0, previousDate);
        }

        Statement statement = new Statement(currentStatement.get(0).trim(),
                adhereToTimeLayout(currentStatement.get(1).trim()),
                Integer.parseInt(currentStatement.get(2)), currentStatement.get(3).trim(),
                String.join(" ", currentStatement.subList(4, currentStatement.size())).trim());

        return statement;
    }

    public String parseLogData(String logData, List<String> statementsToRemove, List<String> usersToRemove,
                               Date startTime, Date endTime)
    {


        List<String> parsedLogStatements = new ArrayList<>();

        List<String> logStatements = new ArrayList<>(Arrays.asList(logData.split("\n")));

        for (String stmt : logStatements)
        {
            // remove the header lines to every db log file
            if (filterHeader(stmt))
            {
                continue;
            }

            // create a statement representation of the line
            Statement statement = createStatement(stmt);
            if (statement.query.equals("Quit") || statement.query.equals("Statistics"))
            {
                continue;
            }

            if (!(isWithinTimeInterval(statement, startTime, endTime)))
            {
                continue;
            }

            // Filters the default rds statements, user-selected users and user-selected statements
            if (filterRDSDefaultStatements(statement) ||
                    filterUserSelectedUsers(statement, usersToRemove) ||
                    idToRemove.contains(statement.id) ||
                    filterUserSelectedStatements(statement, statementsToRemove))
            {
                continue;
            }



            /*
            // Filters the user-selected users and statements
            if (filterUserSelectedUsers(statement, usersToRemove) ||
                    idToRemove.contains(statement.id) ||
                    filterUserSelectedStatements(statement, statementsToRemove))
            {
                continue;
            }*/

            // if the statement passed all filters then add
            parsedLogStatements.add(statement.toString());

        }


        return String.join(",\n", parsedLogStatements);
    }

    private boolean isWithinTimeInterval(Statement statement, Date startTime, Date endTime)
    {

        if (statement.date.equals("------") && statement.time.equals("--:--:--")) {
            return true;
        }

        String pattern = "yymmdd hh:mm:ss";
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date statementDate = sdf.parse(statement.date + " " + statement.time);

            if (statementDate.compareTo(startTime) > 0)
            {
                if (endTime != null)
                {
                    return (statementDate.compareTo(endTime) < 0);
                }
                return true;
            }
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }

        return false;
    }
}
