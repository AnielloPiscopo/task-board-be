package com.example.task_board_be.pojo.resource;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskResourceTest {

    @Test
    void testNoArgs_setters_getters_toString() {
        TaskResource tr = new TaskResource();
        tr.setId(11L);
        tr.setName("Fix bug");
        tr.setDescription("Null ptr");
        tr.setStatus(TaskStatus.TODO);
        tr.setIcon(TaskIcon.BUG);
        tr.setBoardId(3L);

        assertEquals(11L, tr.getId());
        assertEquals("Fix bug", tr.getName());
        assertEquals("Null ptr", tr.getDescription());
        assertEquals(TaskStatus.TODO, tr.getStatus());
        assertEquals(TaskIcon.BUG, tr.getIcon());
        assertEquals(3L, tr.getBoardId());
        assertNotNull(tr.toString());
    }

    @Test
    void testCtor_name_desc_status_icon() {
        TaskResource tr = new TaskResource("A", "B", TaskStatus.DONE, TaskIcon.FEATURE);
        assertNull(tr.getId());
        assertEquals("A", tr.getName());
        assertEquals("B", tr.getDescription());
        assertEquals(TaskStatus.DONE, tr.getStatus());
        assertEquals(TaskIcon.FEATURE, tr.getIcon());
    }

    @Test
    void testCtor_id_name_desc_status_icon() {
        TaskResource tr = new TaskResource(7L, "N", "D", TaskStatus.IN_PROGRESS, TaskIcon.MAINTENANCE);
        assertEquals(7L, tr.getId());
        assertEquals("N", tr.getName());
        assertEquals("D", tr.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tr.getStatus());
        assertEquals(TaskIcon.MAINTENANCE, tr.getIcon());
    }
}
