package app.cli;

import app.models.Capture;

import java.io.IOException;
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
        System.out.println("Please enter the number corresponding to the value you would like >> ");
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

    public static void main(String[] args) {
        Driver driver = new Driver();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to MyCRT!");
        driver.printCommandsList();
        System.out.print("Enter a command >> ");

        while (true) {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] line = input.split(" ");
                switch (line[0]) {
                    case RUN_CAPTURE:
                        driver.runCapture(line);
                        break;
                    case END_CAPTURE:
                        driver.endCapture(line);
                        break;
                    case RUN_REPLAY:
                        driver.runReplay(line);
                        break;
                    case END_REPLAY:
                        driver.end_replay(line);
                        break;
                    case STATUS:
                        driver.status(line);
                        break;
                    case GET_LIST:
                        driver.getList(line);
                        break;
                    case HELP:
                        driver.printCommandsList();
                        break;
                    default:
                        break;

                }

                System.out.print("\nEnter a command >> ");
            }
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
        String rds = line[2];
        String rdsRegion = line[3];
        String s3 = line[4];
        String s3Region = line[5];
        Date startTime = null;
        Date endTime = null;
        long fileSize = 0;
        long transactionSize = 0;

        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;


        for (int i = 6; i < line.length; i+=2) {
            if (i + 1 >= line.length) {
                System.out.println("Here " + i + " length " + line.length);
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm:aa");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(enddate);
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
            CaptureCLI.start(id, rdsRegion, rds, s3Region, s3, startTime, endTime, transactionSize, fileSize, filterStatements, filterUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runCaptureInteractive(String[] line) {
        String id = null;
        String rds = null;
        String rdsRegion = null;
        String s3 = null;
        String s3Region = null;
        Date startTime = null;
        Date endTime = null;
        long fileSize = 0;
        long transactionSize = 0;

        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;

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
            System.out.println("An error occurred when attempting to retrieve S3 buckets at " + rdsRegion
                    + " region");
            return;
        }
        System.out.println("List of available S3 buckets.");
        responseIndex = promptList(scanner, s3Buckets);
        if (responseIndex == -1) {
            return;
        }
        s3 = s3Buckets.get(responseIndex);
        for (int i = 2; i < line.length; i++) {
            if (i + 1 >= line.length) {
                System.out.println("Here " + i + " length " + line.length);
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm:aa");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(enddate);
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
            CaptureCLI.start(id, rdsRegion, rds, s3Region, s3, startTime, endTime, transactionSize, fileSize,
                    filterStatements, filterUsers);
        } catch (IOException e) {
            System.out.println("An error occurred sending your capture. Please try again.");
            return;
        }


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

        if (line.length < 8) {
            commandError(line[0]);
            return;
        }


        String id = line[1];
        String capture_id = line[2];
        String rds = line[3];
        String rdsRegion = line[4];
        String s3 = line[5];
        String s3Region = line[6];
        String mode = line[7];
        String dbUsername = null; // add switch statement support
        String dbPassword = null;
        Date startTime = null;
        Date endTime = null;
        long transactionSize = 0;

        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;

        if (mode.equals("fast-mode")) {
            mode = "Fast Mode";
        } else {
            System.out.println(mode);
        }

        for (int i = 8; i < line.length; i++) {
            if (i + 1 >= line.length) {
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(enddate);
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
            ReplayCLI.start(id, rdsRegion, rds, s3Region, s3, mode, capture_id, startTime, endTime, transactionSize, filterStatements, filterUsers, dbUsername, dbPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Date startTime = null;
        Date endTime = null;
        long transactionSize = 0;

        ArrayList<String> filterStatements = null;
        ArrayList<String> filterUsers = null;

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

        System.out.println("What is the username of the database?");
        if (scanner.hasNextLine()) {
            dbUsername = scanner.nextLine();
        }
        System.out.println("What is the password of the database?");
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
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to retrieve S3 buckets at " + rdsRegion
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

        for (int i = 2; i < line.length; i++) {
            if (i + 1 >= line.length) {
                System.out.println("Here " + i + " length " + line.length);
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start": {
                    String startdate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(startdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "-end": {
                    String enddate = line[i + 1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-hh:mm");
                    try {
                        startTime = dateFormat.parse(enddate);
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
            ReplayCLI.start(id, rdsRegion, rds, s3Region, s3, mode, capture_id, startTime, endTime, transactionSize,
                    filterStatements, filterUsers, dbUsername, dbPassword);
        } catch (IOException e) {
            System.out.println("An error occurred when sending your Replay. Please try again.");
            return;
        }
    }

    private void end_replay(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            return;
        }

        if (line.length < 5) {
            commandError(line[0]);
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

    private void getList(String[] line) {
        if (line.length == 2 && line[1].equals("help")) {
            return;
        }

        if (line.length > 0 && line.length < 3) {
            commandError(line[0]);
        }

        switch (line[1]) {
            case "-rds":
                try {
                    List<String> rdsList = ResourceCLI.rds(line[2]);
                    CLI.presentListOptions(rdsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "-regions":
                try {
                    List<String> rdsList = ResourceCLI.regions();
                    CLI.presentListOptions(rdsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "-s3":
                try {
                    List<String> rdsList = ResourceCLI.s3(line[2]);
                    CLI.presentListOptions(rdsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }


    private void printCommandsList() {
        System.out.println("\nlist of Commands");
        System.out.println("\truncp [capture_name][rds_endpoint] [rds_region] [S3_bucket] [S3_region] -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendcp [capture_name]");
        System.out.println("\trunrp [replay_name] [capture_name] [rds_endpoint] [rds_region] [S3_bucket] [S3_region] [mode] -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendcp [replay_name]");
        System.out.println("\tget -replays -captures -rds -s3 -regions");
        System.out.println("\tstatus -replay -capture");
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
        System.out.println("\t(optional) -start\t\t\tmonth/day/year-hour:minute:AM/PM");
        System.out.println("\t(optional) -end\t\t\t\tmonth/day/year-hour:minute:AM/PM");
        System.out.println("\t(optional) -filesize\t\tSize of the capture file");
        System.out.println("\t(optional) -transnum\t\tNumber of transactions to capture");
        System.out.println("\t(optional) -dbcomignore\t\tDatabase commands to ignore separated by a space, ending with a ' ! '");
        System.out.println("\t(optional) -dbuserignore\tDatabase users to ignore separated by a space, ending with a ' ! '");
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
