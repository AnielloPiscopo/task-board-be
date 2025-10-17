package com.example.task_board_be.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerUtilsTest {

    private String callStart(boolean exceptionFlag) {
        return LoggerUtils.getStandardLoggerMsg("start", exceptionFlag);
    }
    private String callEnd(boolean exceptionFlag) {
        return LoggerUtils.getStandardLoggerMsg("end", exceptionFlag);
    }
    private String callProgress(boolean exceptionFlag) {
        return LoggerUtils.getStandardLoggerMsg("progress", exceptionFlag);
    }
    private String callDefault(boolean exceptionFlag) {
        return LoggerUtils.getStandardLoggerMsg("something-else", exceptionFlag);
    }

    @Test
    void testGetStandardLoggerMsg_start_normal() {
        String msg = callStart(false);
        assertTrue(msg.contains("[LoggerUtilsTest] - [callStart]"));
        assertTrue(msg.endsWith(" - [START]"));
        assertFalse(msg.contains("[EXCEPTION]"));
    }

    @Test
    void testGetStandardLoggerMsg_end_normal() {
        String msg = callEnd(false);
        assertTrue(msg.contains("[LoggerUtilsTest] - [callEnd]"));
        assertTrue(msg.endsWith(" - [END]"));
    }

    @Test
    void testGetStandardLoggerMsg_progress_normal() {
        String msg = callProgress(false);
        assertTrue(msg.contains("[LoggerUtilsTest] - [callProgress]"));
        assertTrue(msg.endsWith(" - [PROGRESS]"));
    }

    @Test
    void testGetStandardLoggerMsg_default_branch_noSuffix() {
        String msg = callDefault(false);
        assertTrue(msg.contains("[LoggerUtilsTest] - [callDefault]"));
        assertFalse(msg.endsWith(" - [START]"));
        assertFalse(msg.endsWith(" - [END]"));
        assertFalse(msg.endsWith(" - [PROGRESS]"));
    }

    @Test
    void testGetStandardLoggerMsg_caseInsensitivePosition() {
        String msg = LoggerUtils.getStandardLoggerMsg("sTaRt", false);
        assertTrue(msg.endsWith(" - [START]"));
    }

    @Test
    void testGetStandardLoggerMsg_exceptionFlagTrue() {
        String msg = callStart(true);
        assertTrue(msg.contains("[LoggerUtilsTest] - [callStart] - [EXCEPTION]"));
        assertTrue(msg.endsWith(" - [START]"));
    }
}
