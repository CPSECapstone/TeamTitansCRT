package app.models;

import java.util.Date;
import java.util.List;

public interface Session {

    public String getId();

    public String getRds();

    public String getS3();

    public String getRdsRegion();

    public String getS3Region();

    public Date getStartTime();

    public Date getEndTime();

    public String getStatus();

    public List<String> getFilterStatements();

    public List<String> getFilterUsers();

    public int getTransactionLimit();
}
