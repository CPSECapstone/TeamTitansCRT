package app.cli;

import app.models.Capture;
import app.models.Replay;
import app.servlets.AnalysisServlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.*;


/*
    TODO: Update "help" instructions for CLI and usage instructions.
    TODO: Add verification of database credentials before continuing in Replay
    TODO: Update optional arguments for capture and replay
*/

public class Driver {

    public static final String RUN_CAPTURE = "runcp";
    public static final String END_CAPTURE = "endcp";
    public static final String RUN_REPLAY = "runrp";
    public static final String END_REPLAY = "endrp";
    public static final String STATUS = "status";
    public static final String GET_LIST = "get";
    public static final String METRICS = "metrics";
    public static final String HELP = "help";

    // returns the index the user selected
    private int promptList(Scanner scanner, List<String> listToPresent) {
        int index = -1;
        if (listToPresent == null || scanner == null || listToPresent.size() == 0) {
            return index;
        }
        ListIterator<String> it = listToPresent.listIterator();
        while (it.hasNext()) {
            System.out.println(it.nextIndex() + ": " + it.next());
        }
        System.out.print("Please enter the number corresponding to the value you would like >> ");
        if (scanner.hasNextLine()) {
            String response = scanner.nextLine();
            if (response.toLowerCase().equals("quit")) {
                return index;
            }
            try {
                index = Integer.parseInt(response);
            } catch (NumberFormatException nfe) {
                return index;
            }
            if (index >= 0 && index < listToPresent.size()) {
                return index;
            }
        }

        return index;
    }

    /*private String regionsPrompt(Scanner scanner) {
        List<String> regions;
        try {
            regions = ResourceCLI.regions();
        } catch (Exception e) {
            System.out.println("An error occurred when attempting to retrieve regions. Please try again.");
            return null;
        }
        while (true) {
            CLI.presentListOptions(regions);
            System.out.print("Please enter the digit of the region >> ");
            if (scanner.hasNextLine()) {
                String response = scanner.nextLine();
                if (response.toLowerCase().equals("quit")) {
                    System.out.println("Quitting interactive mode...");
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(response);
                } catch (NumberFormatException nfe) {
                    System.out.println(response + " is not recognized");
                    continue;
                }
                if (index >= 0 && index < regions.size()) {
                    return regions.get(index);
                } else {
                    System.out.println(response + " is not recognized");
                }
            }
        }
    }*/

    public void startCommandPrompt() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to MyCRT!");
        printCommandsList();
        System.out.print("Enter a command >> ");

        while (true) {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if (input.toLowerCase().equals("quit") || input.toLowerCase().equals("exit")) {
                    System.out.println("Quitting the MyCRT CLI...");
                    break;
                }
                handleCommand(input);
            }
            System.out.print("\nEnter a command >> ");
        }

    }


    public void processCommandFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.isFile()) {
                System.out.println("Cannot find file");
                return;
            }
            FileReader reader = new FileReader(file);
            BufferedReader buffReader = new BufferedReader(reader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                handleCommand(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("An error occurred when processing file.");
            return;
        }
    }

    public void handleCommand(String command) {
        String[] line = command.split(" ");
        switch (line[0]) {
            case RUN_CAPTURE:
                runCapture(line);
                break;
            case END_CAPTURE:
                endCapture(line);
                break;
            case RUN_REPLAY:
                runReplay(line);
                break;
            case END_REPLAY:
                end_replay(line);
                break;
            case STATUS:
                status(line);
                break;
            case GET_LIST:
                getList(line);
                break;
            case METRICS:
                getMetrics(line);
                break;
            case HELP:
                printCommandsList();
                break;
            default:
                System.out.println("Command not recognized");
                break;
        }
    }

    public static void main(String[] args) {
        Driver driver = new Driver();

        if (args.length >= 1) {
            driver.processCommandFile(args[0]);
        }
        else {
            driver.startCommandPrompt();
        }

    }

    private void handleCaptureOptionals(String[] line, int startIndex, String id, String rds, String rdsRegion,
                                        String s3, String s3Region) {
        Date startTime = null;
        Date endTime = null;
        long fileSize = 0;
        long transactionSize = 0;
        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;


        for (int i = startIndex; i < line.length; i+=2) {
            if (i + 1 >= line.length) {
                System.out.println("Here " + i + " length " + line.length);
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mmaa");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mmaa");
                    try {
                        endTime = dateFormat.parse(enddate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-filesize":
                    String size = line[i + 1];
                    fileSize = Long.parseLong(size);
                    break;
                case "-transnum":
                    size = line[i + 1];
                    transactionSize = Long.parseLong(size);
                    break;
                case "-dbcomignore":
                    break;
                case "dbuserignore":
                    break;
                default:
                    break;
            }

        }

        try {
            CaptureCLI.start(id, rdsRegion, rds, s3Region, s3, startTime, endTime, transactionSize,
                    fileSize, filterStatements, filterUsers);
        } catch (IOException e) {
            System.out.println("An error occurred sending your capture. Please try again.");
            return;
        }
    }

    private void handleReplayOptionals(String[] line, int startIndex, String capture_id, String id, String rds, String rdsRegion,
                                       String s3, String s3Region, String mode, String dbUsername, String dbPassword) {
        Date startTime = null;
        Date endTime = null;
        long fileSize = 0;
        long transactionSize = 0;
        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;

        for (int i = startIndex; i < line.length; i++) {
            if (i + 1 >= line.length) {
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mmaa");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mmaa");
                    try {
                        endTime = dateFormat.parse(enddate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-transize":
                    String size = line[i + 1];
                    transactionSize = Long.parseLong(size);
                    break;
                default:
                    break;
            }

        }

        try {
            ReplayCLI.start(id, rdsRegion, rds, s3Region, s3, mode, capture_id, startTime, endTime,
                    transactionSize, filterStatements, filterUsers, dbUsername, dbPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runCapture(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            printRunCaptureHelp();
            return;
        }
        else if (line.length >= 2 && line[1].equals("-i")) {
            runCaptureInteractive(line);
            return;
        }

        if (line.length < 6) {
            commandError(line[0]);
            return;
        }

        String id = line[1];
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("Replay name can only contain a-z, A-Z, 0-9");
            System.out.println("Quitting...");
            return;
        }
        String rds = line[2];
        String rdsRegion = line[3];
        String s3 = line[4];
        String s3Region = line[5];

        //checks if the user input is actually correct

        //get regions
        List<String> regions;
        try {
            regions = ResourceCLI.regions();
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve regions. Please try again.");
            return;
        }

        //check if rds region is valid
        if(!regions.contains(rdsRegion)) {
            System.out.println("Could not find rds region: " + rdsRegion);
            return;
        }
        else {
            //get rdsInstances
            List<String> rdsInstances;
            try {
                rdsInstances = ResourceCLI.rds(rdsRegion);
                //check if rds instance is valid
                if(!rdsInstances.contains(rds)) {
                    System.out.println("Could not find rds instance: " + rds);
                    return;
                }
            } catch (IOException e) {
                System.out.println("An error occurred when attempting to retrieve RDS instances at " + rdsRegion
                        + " region");
                return;
            }
        }
        //check if s3 region is valid
        if(!regions.contains(s3Region)) {
            System.out.println("Could not find S3 region: " + s3Region);
            return;
        }
        else {
            //get s3 buckets
            List<String> s3Buckets;
            try {
                s3Buckets = ResourceCLI.s3(s3Region);
                //check if s3 bucket is valid
                if(!s3Buckets.contains(s3)) {
                    System.out.println("Could not find S3 bucket: " + s3);
                    return;
                }
            } catch (IOException e) {
                System.out.println("An error occurred when attempting to retrieve S3 buckets at " + s3Region
                        + " region");
                return;
            }
        }
        //all inputs are valid
        handleCaptureOptionals(line, 6, id, rds, rdsRegion, s3, s3Region);
    }

    private void runCaptureInteractive(String[] line) {
        String id = null;
        String rds = null;
        String rdsRegion = null;
        String s3 = null;
        String s3Region = null;

        int responseIndex;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter a capture name >> ");
            if (scanner.hasNextLine()) {
                id = scanner.nextLine();
                if (id.toLowerCase().equals("quit")) {
                    System.out.println("Quitting interactive mode...");
                    return;
                }
                break;
            }
        }
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("Replay name can only contain a-z, A-Z, 0-9");
            System.out.println("Quitting...");
            return;
        }
        System.out.println("Configuring RDS....");
        List<String> regions;
        try {
            regions = ResourceCLI.regions();
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve regions. Please try again.");
            return;
        }
        System.out.println("What region is the RDS instance in?");
        responseIndex = promptList(scanner, regions);
        if (responseIndex == -1) {
            return;
        }
        rdsRegion = regions.get(responseIndex);
        List<String> rdsInstances;
        try {
            rdsInstances = ResourceCLI.rds(rdsRegion);
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve RDS instances at " + rdsRegion
                    + " region");
            return;
        }
        System.out.println("What RDS would you like to capture on?");
        responseIndex = promptList(scanner, rdsInstances);
        if (responseIndex == -1) {
            return;
        }
        rds = rdsInstances.get(responseIndex);

        System.out.println("What region is the S3 bucket in?");
        responseIndex = promptList(scanner, regions);
        if (responseIndex == -1) {
            return;
        }
        s3Region = regions.get(responseIndex);
        List<String> s3Buckets;
        try {
            s3Buckets = ResourceCLI.s3(s3Region);
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve S3 buckets at " + s3Region
                    + " region");
            return;
        }
        System.out.println("List of available S3 buckets.");
        responseIndex = promptList(scanner, s3Buckets);
        if (responseIndex == -1) {
            return;
        }
        s3 = s3Buckets.get(responseIndex);

        handleCaptureOptionals(line, 2, id, rds, rdsRegion, s3, s3Region);
    }

    private void endCapture(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            printEndCaptureHelp();
            return;
        }

        if (line.length == 2) {
            String captureIDString = line[1];
            try {
                CaptureCLI.stop(captureIDString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error - Too many arguments");
        }
    }

    private void runReplay(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            printRunReplayHelp();
            return;
        }
        else if (line.length >= 2 && line[1].equals("-i")) {
            runReplayInteractive(line);
            return;
        }

        if (line.length < 10) {
            commandError(line[0]);
            return;
        }


        String id = line[1];
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("Replay name can only contain a-z, A-Z, 0-9");
            System.out.println("Quitting...");
            return;
        }
        String capture_id = line[2];
        String rds = line[3];
        String rdsRegion = line[4];
        String s3 = line[5];
        String s3Region = line[6];
        String mode = line[7];
        String dbUsername = line[8];
        String dbPassword = line[9];


        if (mode.equals("fast-mode")) {
            mode = "Fast Mode";
        } else {
            System.out.println(mode);
        }

        handleReplayOptionals(line, 10, capture_id, id, rds, rdsRegion, s3, s3Region, mode, dbUsername, dbPassword);
    }

    private void runReplayInteractive(String[] line) {
        String id = null;
        String capture_id = null;
        String rds = null;
        String rdsRegion = null;
        String s3 = null;
        String s3Region = null;
        String mode = null;
        String dbUsername = null;
        String dbPassword = null;

        Scanner scanner = new Scanner(System.in);
        int responseIndex;

        while (true) {
            List<Capture> captures;
            try {
                captures = ResourceCLI.captures("Finished");
            } catch (IOException e) {
                System.out.println("An error occurred when attempting to retrieve regions. Please try again.");
                return;
            }
            CLI.presentCaptureIdListOptions(captures);
            System.out.print("Please select the digit of the capture to replay >> ");
            if (scanner.hasNextLine()) {
                int index = Integer.parseInt(scanner.nextLine());
                if (index >= 0 && index < captures.size()) {
                    capture_id = captures.get(index).getId();
                    break;
                }
            }
        }

        while (true) {
            System.out.print("Please enter a replay name >> ");
            if (scanner.hasNextLine()) {
                id = scanner.nextLine();
                if (id.toLowerCase().equals("quit")) {
                    System.out.println("Quitting interactive mode...");
                    return;
                }
                break;
            }
        }
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("Replay name can only contain a-z, A-Z, 0-9");
            System.out.println("Quitting...");
            return;
        }
        System.out.println("Configuring RDS....");
        List<String> regions;
        try {
            regions = ResourceCLI.regions();
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve regions. Please try again.");
            return;
        }
        System.out.println("What region is the RDS instance in?");
        responseIndex = promptList(scanner, regions);
        if (responseIndex == -1) {
            return;
        }
        rdsRegion = regions.get(responseIndex);
        List<String> rdsInstances;
        try {
            rdsInstances = ResourceCLI.rds(rdsRegion);
            if (rdsInstances.size() == 0) {
                System.out.println("There are no rds instances at the selected region: "  + rdsRegion);
                return;
            }
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve RDS instances at " + rdsRegion
                    + " region");
            return;
        }
        System.out.println("What RDS would you like to capture on?");
        responseIndex = promptList(scanner, rdsInstances);
        if (responseIndex == -1) {
            return;
        }
        rds = rdsInstances.get(responseIndex);

        System.out.print("What is the username of the database?");
        if (scanner.hasNextLine()) {
            dbUsername = scanner.nextLine();
        }
        System.out.print("What is the password of the database?");
        if (scanner.hasNextLine()) {
            dbPassword = scanner.nextLine();
        }

        if (dbUsername == null && dbPassword == null) {
            System.out.println("Invalid username and password. Quitting interactive mode.");
        }

        System.out.println("What region is the S3 bucket in?");
        responseIndex = promptList(scanner, regions);
        if (responseIndex == -1) {
            return;
        }
        s3Region = regions.get(responseIndex);
        List<String> s3Buckets;
        try {
            s3Buckets = ResourceCLI.s3(s3Region);
            if (s3Buckets.size() == 0) {
                System.out.println("There are no S3 at the selected region: "  + s3Region);
                return;
            }
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve S3 buckets at " + s3Region
                    + " region");
            return;
        }
        System.out.println("List of available S3 buckets.");
        responseIndex = promptList(scanner, s3Buckets);
        if (responseIndex == -1) {
            return;
        }
        s3 = s3Buckets.get(responseIndex);

        System.out.println("What mode would you like for this replay?");
        List<String> replayModes = Arrays.asList("Fast Mode", "Time Sensitive");
        responseIndex = promptList(scanner, replayModes);
        if (responseIndex == -1) {
            return;
        }
        mode = replayModes.get(responseIndex);

        handleReplayOptionals(line, 2, capture_id, id, rds, rdsRegion, s3, s3Region, mode, dbUsername,
                dbPassword);

    }

    private void end_replay(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            return;
        }

        if (line.length == 2) {
            String replayIDString = line[1];
            try {
                ReplayCLI.stop(replayIDString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error - Too many arguments");
        }
    }

    private void status(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            return;
        }

        if (line.length < 5) {
            commandError(line[0]);
        }
    }

    private void getMetrics(String[] line) {
        if(line.length >= 3) {
            try {
                List<Capture> captureList = ResourceCLI.captures();
                String captureName = line[1];
                String[] metricsList = Arrays.copyOfRange(line, 2, line.length);
                Capture capture = null;
                for (Capture c : captureList){
                    if(captureName.equals(c.getId()) && "Running".equals(c.getStatus())){
                        capture = c;
                        break;
                    }
                }
                if(capture == null) {
                    System.out.println("This capture has already ended or does not exist.");
                }
                else {
                    System.out.println(AnalysisCLI.average(capture, metricsList));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            commandError(line[0]);
        }
    }

    private void getList(String[] line) {
        if(line.length < 2 || line.length > 3) {
            System.out.println("Error - Incorrect input. See 'get help' for details on get");
            return;
        }
        else if (line[1].equals("help")) {
            printGetHelp();
            return;
        }

        switch (line[1]) {
            case "-rds":
                if (line.length > 0 && line.length < 3) {
                    System.out.println("This command requires a region.");
                    commandError(line[0]);
                    break;
                }
                try {
                    List<String> rdsList = ResourceCLI.rds(line[2]);
                    CLI.presentListOptions(rdsList);
                } catch (ConnectException ce) {
                    System.out.println("Connection refused");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "-regions":
                try {
                    List<String> rdsList = ResourceCLI.regions();
                    CLI.presentListOptions(rdsList);
                } catch (ConnectException ce) {
                    System.out.println("Connection refused");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "-s3":
                if (line.length > 0 && line.length < 3) {
                    System.out.println("This command requires a region.");
                    commandError(line[0]);
                    break;
                }
                try {
                    List<String> rdsList = ResourceCLI.s3(line[2]);
                    CLI.presentListOptions(rdsList);
                } catch (ConnectException ce) {
                    System.out.println("Connection refused");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "-captures":
                try {
                    List<Capture> captureList = ResourceCLI.captures();
                    CLI.presentCaptureIdListOptions(captureList);
                } catch (ConnectException ce) {
                    System.out.println("Connection refused");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "-replays":
                try {
                    List<Replay> replayList = ResourceCLI.replays();
                    CLI.presentReplayIdListOptions(replayList);
                } catch (ConnectException ce) {
                    System.out.println("Connection refused");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Error - Incorrect input. See 'get help' for details on get");
                break;
        }

    }


    private void printCommandsList() {
        System.out.println("\nlist of Commands");
        System.out.println("\truncp [capture_name][rds_endpoint] [rds_region] [S3_bucket] [S3_region] -i -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendcp [capture_name]");
        System.out.println("\trunrp [replay_name] [capture_name] [rds_endpoint] [rds_region] [S3_bucket] [S3_region] [mode] [dbUsername] [dbPassword] -i -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendrp [replay_name]");
        System.out.println("\tget -replays -captures -rds -s3 -regions");
        System.out.println("\tstatus -replay -capture");
        System.out.println("\tmetrics [capture_name] [metric_name]");
        System.out.println("\thelp");
        System.out.println("");
    }

    private void printRunCaptureHelp() {
        System.out.println("\nruncp");
        System.out.println("\tcapture_name\t\t\t\tThe name of the capture.");
        System.out.println("\trds_endpoint\t\t\t\tThe name of your rds database.");
        System.out.println("\trds_region\t\t\t\t\tThe region your database is in.");
        System.out.println("\tS3_bucket\t\t\t\t\tThe S3 bucket to store the capture metrics in");
        System.out.println("\tS3_region\t\t\t\t\tThe region your S3 bucket is in");
        System.out.println("\t(optional) -i\t\t\t\tStart in interactive mode. Other optional flags allowed.");
        System.out.println("\t(optional) -start\t\t\tmonth/day/year-hour:minuteAM/PM");
        System.out.println("\t(optional) -end\t\t\t\tmonth/day/year-hour:minuteAM/PM");
        System.out.println("\t(optional) -filesize\t\tSize of the capture file");
        System.out.println("\t(optional) -transnum\t\tNumber of transactions to capture");
        System.out.println("\t(optional) -dbcomignore\t\tDatabase commands to ignore separated by a space, ending with a ' ! '");
        System.out.println("\t(optional) -dbuserignore\tDatabase users to ignore separated by a space, ending with a ' ! '");
        System.out.println();
    }

    private void printGetHelp() {
        System.out.println("\nget");
        System.out.println("\t-replays\t\t\tList all replays.");
        System.out.println("\t-captures\t\tList all captures.");
        System.out.println("\t-rds -region\t\tList all rds instances. -rds is a flag.");
        System.out.println("\t-s3 -region\t\tList all s3 buckets. -s3 is a flag.");
        System.out.println();
    }

    private void printEndCaptureHelp() {
        System.out.println("endcp instructions");
    }

    private void printRunReplayHelp() {
        System.out.println("runrp instructions");
        System.out.println("\t(optional) -i\t\t\t\tStart in interactive mode. Other optional flags allowed.");
    }


    private void commandError(String command) {
        System.out.println("Error - Incorrect input. See '" + command + " help' for details on " + command);
    }
}
