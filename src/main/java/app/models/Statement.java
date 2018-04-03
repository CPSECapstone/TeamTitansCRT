package app.models;

public class Statement  {
    private String date;
    private String time;
    private int id;
    private String command;
    private String query;

    public Statement(String date, String time, int id, String command, String query)
    {
        this.date = date;
        this.time = time;
        this.id = id;
        this.command = command;
        this.query = query;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String getQuery() {
        return query;
    }

    public String toString()
    {
        return "{\n" + "\"date\": \"" + this.date + "\",\n\"time\": \"" + this.time +
                "\",\n\"id\": " + this.id + ",\n\"command\": \"" + this.command +
                "\",\n\"query\": \"" + this.query + "\"\n}";
    }


}
