package app.cli;

import app.models.Capture;

import java.io.IOException;
import java.util.Scanner;
import java.util.Date;
import java.util.ArrayList;

public class Driver {

    public static final String RUN_CAPTURE = "runcp";
    public static final String END_CAPTURE = "endcp";
    public static final String RUN_REPLAY = "runrp";
    public static final String END_REPLAY = "endrp";
    public static final String STATUS = "status";
    public static final String GET_LIST = "get";


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
                    default:
                        driver.printCommandsList();
                        break;

                }

                System.out.print("Enter a command >> ");

            }
        }
    }

    private void runCapture(String[] line) {
        if (line.length == 2 && line[1] == "help") {
            printRunCaptureHelp();
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


        for (int i = 6; i < line.length; i++) {
            if (i + 1 >= line.length) {
                commandError(line[0]);
                return;
            }
            switch (line[i]) {
                case "-start":
                    break;
                case "-end":
                    break;
                case "-filesize":
                    String size = line[i + 1];
                    fileSize = Long.parseLong(size);
                    break;
                case "-transize":
                    size = line[i + 1];
                    transactionSize = Long.parseLong(size);
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

    private void endCapture(String[] line) {
        if (line.length == 2 && line[1] == "help") {
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
        if (line.length == 2 && line[1] == "help") {
            printRunReplayHelp();
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
                case "-start":
                    break;
                case "-end":
                    break;
                case "-transize":
                    String size = line[i + 1];
                    transactionSize = Long.parseLong(size);
                    break;
                default:
                    break;
            }

        }

        try {
            ReplayCLI.start(id, rdsRegion, rds, s3Region, s3, mode, capture_id, startTime, endTime, transactionSize, filterStatements, filterUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void end_replay(String[] line) {
        if (line.length == 2 && line[1] == "help") {
            return;
        }

        if (line.length < 5) {
            commandError(line[0]);
        }
    }

    private void status(String[] line) {
        if (line.length == 2 && line[1] == "help") {
            return;
        }

        if (line.length < 5) {
            commandError(line[0]);
        }
    }

    private void getList(String[] line) {
        if (line.length == 2 && line[1] == "help") {
            return;
        }

        if (line.length < 5) {
            commandError(line[0]);
        }
    }


    private void printCommandsList() {
        System.out.println("\nlist of Commands");
        System.out.println("\truncp [capture_name][rds_endpoint] [rds_region] [S3_bucket] [S3_region] -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendcp [capture_name]");
        System.out.println("\trunrp [replay_name] [capture_name] [rds_endpoint] [rds_region] [S3_bucket] [S3_region] [mode] -start -end -filesize -transize -dbcom -dbuser");
        System.out.println("\tendcp [replay_name]");
        System.out.println("\tget -replays -captures -rds -s3");
        System.out.println("\tstatus -replay -capture");
        System.out.println("\thelp");
        System.out.println("");
    }

    private void printRunCaptureHelp() {
        System.out.println("runcp instructions");
    }

    private void printEndCaptureHelp() {
        System.out.println("endcp instructions");
    }

    private void printRunReplayHelp() {
        System.out.println("runrp instructions");
    }


    private void commandError(String command) {
        System.out.println("Error - Incorrect input. See '" + command + " help' for details on " + command);
    }
}
