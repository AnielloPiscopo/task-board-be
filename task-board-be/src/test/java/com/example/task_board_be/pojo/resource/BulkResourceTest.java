package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.BulkOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulkResourceTest {

    @Test
    void testNoArgs_setters_getters_toString() {
        BulkResource br = new BulkResource();
        br.setOperation(BulkOperation.ARCHIVE_LIST);
        br.setUpdatedRow(7);

        assertEquals(BulkOperation.ARCHIVE_LIST, br.getOperation());
        assertEquals(7, br.getUpdatedRow());
        assertNotNull(br.toString());
    }

    @Test
    void testCtor_full() {
        BulkResource br = new BulkResource(BulkOperation.DELETE_LIST, 3);
        assertEquals(BulkOperation.DELETE_LIST, br.getOperation());
        assertEquals(3, br.getUpdatedRow());
    }
}
