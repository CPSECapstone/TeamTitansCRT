package app.util;

public class ErrorsUtil {

    public static String DuplicateCaptureIDError() {
        return "Error: Duplicate Capture ID";
    }

    public static String DuplicateReplayIDError() {
        return "Error: Duplicate Replay ID";
    }


    public static String StartTimeBeforeCurrentTimeError() {
        return "Error: Start time is before the current time";
    }

    public static String EndTimeBeforeStartTimeError() {
        return "Error: End time is before the starting time";
    }

    public static String NegativeNumbersError() {
        return "Error: Negative numbers are not allowed";
    }

    public static String idContainsNonAlphaNumeric() {
        return "Error: Capture ID can only contain alphanumeric characters";
    }

}
