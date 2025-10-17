package com.example.task_board_be.pojo.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResourceTest {

    @Test
    void testNoArgs_hasTimestamp_and_setters() {
        ErrorResource er = new ErrorResource();
        assertNotNull(er.getTimestamp());

        er.setStatus(500);
        er.setMessage("x");

        assertEquals(500, er.getStatus());
        assertEquals("x", er.getMessage());
        assertNotNull(er.toString());
    }

    @Test
    void testCtor_status_message_setsTimestamp() {
        ErrorResource er = new ErrorResource(404, "not found");
        assertEquals(404, er.getStatus());
        assertEquals("not found", er.getMessage());
        assertNotNull(er.getTimestamp());
    }
}
