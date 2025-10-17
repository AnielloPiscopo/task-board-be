package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.BulkOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CascadeBulkResourceTest {

    @Test
    void testNoArgs_setters_getters_toString() {
        CascadeBulkResource r = new CascadeBulkResource();
        r.setOperation(BulkOperation.RESTORE_LIST);
        r.setUpdatedRow(4);
        r.setCascade(Boolean.TRUE);

        assertEquals(BulkOperation.RESTORE_LIST, r.getOperation());
        assertEquals(4, r.getUpdatedRow());
        assertEquals(Boolean.TRUE, r.getCascade());
        assertNotNull(r.toString());
    }

    @Test
    void testCtor_full() {
        CascadeBulkResource r = new CascadeBulkResource(BulkOperation.ARCHIVE_LIST, 9, Boolean.FALSE);
        assertEquals(BulkOperation.ARCHIVE_LIST, r.getOperation());
        assertEquals(9, r.getUpdatedRow());
        assertEquals(Boolean.FALSE, r.getCascade());
    }
}
