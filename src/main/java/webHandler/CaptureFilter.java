package webHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
public class CaptureFilter extends LogFilter {
    private String previousTime = ""; // the time of the previous statement
    private String previousDate = ""; // the date of the previous statement
    private Date startTime; // the start time of the capture
    private Date endTime; // the end time of the capture
    private int transactionLimit;
    private String captureID;

    public CaptureFilter(Capture capture)
    {
        this.captureID = capture.getId();
        this.startTime = capture.getStartTime();
        this.endTime = capture.getEndTime();
        this.transactionLimit = capture.getTransactionLimit();
        this.statementsToRemove = capture.getFilterStatements();
        this.usersToRemove = capture.getFilterUsers();
    }
    // filters out the header of every log file
    private boolean isHeader(String line)
    {
        return line.matches("(?i).*tcp port.*") ||
                line.matches("(?i).*rdsdbbin.*") ||
                line.matches("(?i)Time.*Id.*Command.*Argument");
    }
    // Checks to see whether the statement falls within the capture's start time and end time.
    private boolean isWithinTimeInterval(Statement statement, Date startTime, Date endTime)
    {
        // if the date and time are empty then just accept the statement and return
        if (statement.getDate().equals("------") && statement.getTime().equals("--:--:--")) {
            return true;
        }
        String pattern = "yymmdd hh:mm:ss";
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date statementDate = sdf.parse(statement.getDate() + " " + statement.getTime());
            // is the statement later than the start time
            if (statementDate.compareTo(startTime) > 0)
            {
                if (endTime != null)
                {
                    // is the statement before the end time
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
    // filters out all default rds statements
    protected boolean isRDSDefaultStatement(Statement statement)
    {
        // don't think this is an exhaustive list...
        String query = statement.getQuery();
        // returns true if the query matches any of the following
        if (query.equals("SELECT 1") ||
                query.equals("Statistics") ||
                query.equals("COMMIT") ||
                query.equals("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ") ||
                query.equals("SELECT current_user()") ||
                query.equals("SELECT CONNECTION_ID()") ||
                query.equals("SELECT DATABASE()") ||
                query.equals("SELECT NAME FROM mysql.func") ||
                query.equals("SHOW GLOBAL VARIABLES") ||
                query.equals("show charset") ||
                query.equals("show collation") ||
                query.matches("(?i)SELECT .* FROM mysql..*") ||
                query.matches("(?i)SHOW PROCEDURE STATUS WHERE.*") ||
                query.matches("(?i)SHOW FUNCTION STATUS WHERE.*") ||
                query.matches("(?i).*FROM information_schema.*") ||
                query.matches("(?i)SHOW TRIGGERS FROM.*") ||
                query.matches("(?i)SELECT tablespace_name.*") ||
                query.matches("(?i)SELECT logfile_group_name.*") ||
                query.matches("(?i).*rds_.*") ||
                query.matches("(?i).*purge binary logs to.*") ||
                query.matches("(?i).*@@session.*") ||
                query.matches("(?i)show global variables like.*") ||
                query.matches("(?i)flush logs.*") ||
                query.matches("(?i)set autocommit.*") ||
                query.matches("(?i)set sql.*") ||
                query.matches("(?i)set character_set_results.*") ||
                query.matches("(?i)set names.*") ||
                query.matches("(?i)Show engines") ||
                query.matches("(?i)show session.*") ||
                query.matches("(?i)show function status") ||
                query.matches("(?i)show full tables.*"))
        {
            return true;
        }
        return false;
    }
    // Forces time to follow hh:mm:ss
    protected String adhereToTimeLayout(String time)
    {
        String timeFormat = "%02d:%02d:%02d";
        return String.format(timeFormat, Arrays.stream(time.split(":")).map(Integer::parseInt).toArray());
    }
    private Statement createStatement(String stmt)
    {
        // the stmt format is [date, time, id, command, query] all space separated
        List<String> currentStatement = new ArrayList<>(Arrays.asList(stmt.split("\\s"))); // split on whitespace
        currentStatement.removeAll(Arrays.asList("", null)); // remove all empty or null values from list
        // if it already has a time
        if (currentStatement.get(1).split(":").length >= 3)
        {
            previousDate = currentStatement.get(0);
            previousTime = currentStatement.get(1);
        } // default time and date if none available
        else if (previousTime.equals("") || previousDate.equals(""))
        {
            // currently if no time or date is available then use this format
            currentStatement.add(0, "--:--:--");
            currentStatement.add(0, "------");
        }
        else // add the previous statement's time and date if available
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

    public List<Statement> filterLogData(String logData)
    {
        // resulting filtered array of statements
        List<Statement> filteredLogStatements = new ArrayList<>();
        // logData represented as an array of statements
        List<String> logStatements = new ArrayList<>(Arrays.asList(logData.split("\n")));
        // add all the default filtered values
        addDefaultFilterValues();
        for (String stmt : logStatements)
        {
            // if the capture has reached the transaction limit
            if (transactionLimit > 0 && transactionCount >= transactionLimit)
            {
                break;
            }
            // remove the header lines to every db log file
            if (isHeader(stmt))
            {
                continue;
            }
            // create a statement representation of the line
            Statement statement = createStatement(stmt);
            if (statement.getQuery().equals("Quit") || statement.getQuery().equals("Statistics"))
            {
                continue;
            }
            if (!(isWithinTimeInterval(statement, startTime, endTime)))
            {
                continue;
            }
            // Filters the default rds statements, user-selected users and user-selected statements
            if (isRDSDefaultStatement(statement) ||
                    isUserSelectedUser(statement) ||
                    idToRemove.contains(statement.getId()) ||
                    isUserSelectedStatement(statement))
            {
                continue;
            }
            /*
            // Filters the user-selected users and statements
            if (isUserSelectedUser(statement, usersToRemove) ||
                    idToRemove.contains(statement.getId()) ||
                    isUserSelectedStatement(statement, statementsToRemove))
            {
                continue;
            }*/
            // if the statement is not a connect
            if (!(isConnectCommand(statement.getCommand())))
            {
                transactionCount++;
            }
            // if the statement passed all filters then add
            filteredLogStatements.add(statement);
        }

        updateCaptureController();
        return filteredLogStatements;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public void setTransactionLimit(int limit) {
        this.transactionLimit = limit;
    }

    private void updateCaptureController() {
        CaptureController.getInstance().updateCaptureTransactionCount(captureID, transactionCount);
    }
}