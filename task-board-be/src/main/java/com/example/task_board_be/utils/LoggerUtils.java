package com.example.task_board_be.utils;

public class LoggerUtils {
    private LoggerUtils() {
    }

    /// Helper function to extract the current method name from the stack trace
    private static String _getCurrentInfoName() {
        // 0 = _getCurrentInfoName
        // 1 = getLoggerMsg
        // 2 = metodo del chiamante (es. createBoard nel controller)
        StackTraceElement element = new Exception().getStackTrace()[2];

        String fullClassName = element.getClassName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = element.getMethodName();

        return "[" + simpleClassName + "] - [" + methodName + "]";
    }

    public static String getStandardLoggerMsg(String loggerPosition, boolean isExceptionLogger) {
        String loggerMsg = (isExceptionLogger) ? _getCurrentInfoName() + " - [EXCEPTION]" : _getCurrentInfoName();

        return switch (loggerPosition.toUpperCase()) {
            case "START" -> loggerMsg + " - [START]";
            case "END" -> loggerMsg + " - [END]";
            case "PROGRESS" -> loggerMsg + " - [PROGRESS]";
            default -> loggerMsg;
        };
    }
}
