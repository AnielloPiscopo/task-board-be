// src/test/java/com/example/task_board_be/pojo/entity/TaskEntityTest.java
package com.example.task_board_be.pojo.entity;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testConstructorsAndAccessors() {
        Task t1 = new Task();
        t1.setId(7L);
        t1.setName("N");
        t1.setDescription("D");
        t1.setStatus(TaskStatus.NONE);
        t1.setIcon(TaskIcon.NONE);
        assertEquals(7L, t1.getId());
        assertEquals("N", t1.getName());
        assertEquals("D", t1.getDescription());
        assertEquals(TaskStatus.NONE, t1.getStatus());
        assertEquals(TaskIcon.NONE, t1.getIcon());

        Task t2 = new Task("A", "B", TaskStatus.NONE, TaskIcon.NONE);
        assertEquals("A", t2.getName());
        assertEquals("B", t2.getDescription());
        assertEquals(TaskStatus.NONE, t2.getStatus());
        assertEquals(TaskIcon.NONE, t2.getIcon());

        Task t3 = new Task(3L, "C", "E", TaskStatus.NONE, TaskIcon.NONE);
        assertEquals(3L, t3.getId());
        assertEquals("C", t3.getName());
        assertEquals("E", t3.getDescription());

        assertTrue(t3.toString().contains("Task"));
    }

    @Test
    void testBoardBackReference_setAndGet() {
        Board b = new Board();
        b.setId(100L);
        Task t = new Task();
        t.setBoard(b);
        assertSame(b, t.getBoard());
    }
}
