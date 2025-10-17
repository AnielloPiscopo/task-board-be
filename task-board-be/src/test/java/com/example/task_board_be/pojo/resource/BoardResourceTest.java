package com.example.task_board_be.pojo.resource;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardResourceTest {

    @Test
    void testNoArgs_setters_getters_toString() {
        BoardResource br = new BoardResource();
        br.setId(10L);
        br.setName("Roadmap");
        br.setDescription("Q4");
        TaskResource t1 = new TaskResource(1L, "A", "d", null, null);
        TaskResource t2 = new TaskResource(2L, "B", "e", null, null);
        br.setTaskResourceList(Arrays.asList(t1, t2));

        assertEquals(10L, br.getId());
        assertEquals("Roadmap", br.getName());
        assertEquals("Q4", br.getDescription());
        assertEquals(2, br.getTaskResourceList().size());
        assertNotNull(br.toString());
    }

    @Test
    void testCtor_name_desc() {
        BoardResource br = new BoardResource("N", "D");
        assertNull(br.getId());
        assertEquals("N", br.getName());
        assertEquals("D", br.getDescription());
    }

    @Test
    void testCtor_id_name_desc() {
        BoardResource br = new BoardResource(5L, "N2", "D2");
        assertEquals(5L, br.getId());
        assertEquals("N2", br.getName());
        assertEquals("D2", br.getDescription());
    }
}
