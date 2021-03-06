package app.util;

import app.models.Session;
import app.models.Statement;

import java.util.ArrayList;
import java.util.List;

public abstract class LogFilter {
    protected List<Integer> idToRemove = new ArrayList<>(); // list of connection id's to remove
    protected List<String> statementsToRemove; // list of statements to remove
    protected List<String> usersToRemove; // list of user names to remove

    protected int transactionCount = 0; // current number of non-connection transactions successfully filtered
    protected int transactionLimit = 0; // the transaction limit -- default is 0

    // add the default user
    private void addDefaultUserFilterValues()
    {
        if (!usersToRemove.contains("rdsadmin"))
        {
            usersToRemove.add("rdsadmin");
        }
    }

    public void setTransactionCount(int transactionCount)
    {
        this.transactionCount = transactionCount;
    }

    public void setTransactionLimit(int transactionLimit)
    {
        this.transactionLimit = transactionLimit;
    }


    // adds default users and statements
    protected void addDefaultFilterValues()
    {
       addDefaultUserFilterValues();
    }

    protected boolean isConnectCommand(String command)
    {
        return command.equals("Connect");
    }

    protected boolean isUserSelectedStatement(Statement statement)
    {

        for (String statementToRemove : statementsToRemove)
        {

            // Regex match any user-selected statements with current queries
            if (!statementToRemove.equals("") && statement.getQuery().matches("(?i)" + statementToRemove + ".*"))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isUserSelectedUser(Statement statement)
    {
        // Match any users connections with user-selected users
        if (statement.getCommand().equals("Connect") && usersToRemove.contains(statement.getQuery().split("@")[0]))
        {
            idToRemove.add(statement.getId());
            return true;
        }
        return false;
    }

    public abstract List<Statement> filterLogData(String logData);

    public boolean hasReachedTransactionLimit()
    {
        return this.transactionCount > this.transactionLimit;
    }

    public int getTransactionCount()
    {
        return this.transactionCount;
    }

    public abstract void update(Session session);
}
