package webHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface Session {

    public String getId();

    public String getRds();

    public String getS3();

    public Date getStartTime();

    public Date getEndTime();

    public String getStatus();

    public List<String> getFilterStatements();

    public List<String> getFilterUsers();
}
