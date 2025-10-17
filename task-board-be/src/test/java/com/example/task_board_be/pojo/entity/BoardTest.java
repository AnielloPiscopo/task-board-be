// src/test/java/com/example/task_board_be/pojo/entity/BoardEntityTest.java
package com.example.task_board_be.pojo.entity;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testConstructorsAndAccessors() {
        Board b1 = new Board();
        b1.setId(1L);
        b1.setName("N");
        b1.setDescription("D");
        assertEquals(1L, b1.getId());
        assertEquals("N", b1.getName());
        assertEquals("D", b1.getDescription());

        Board b2 = new Board("A", "B");
        assertEquals("A", b2.getName());
        assertEquals("B", b2.getDescription());

        Board b3 = new Board(3L, "C", "E");
        assertEquals(3L, b3.getId());
        assertEquals("C", b3.getName());
        assertEquals("E", b3.getDescription());

        assertTrue(b3.toString().contains("Board"));
    }

    @Test
    void testAddTask_setsBackReference() {
        Board board = new Board();
        board.setId(10L);
        Task t = new Task();
        t.setName("T");
        board.addTask(t);
        assertEquals(1, board.getTaskList().size());
        assertSame(board, t.getBoard());
    }

    @Test
    void testRemoveTask_clearsBackReference() {
        Board board = new Board();
        Task t = new Task();
        board.addTask(t);
        board.removeTask(t);
        assertTrue(board.getTaskList().isEmpty());
        assertNull(t.getBoard());
    }

    @Test
    void testAddTaskList_null_doesNothing() {
        Board board = new Board();
        board.addTaskList(null);
        assertTrue(board.getTaskList().isEmpty());
    }

    @Test
    void testAddTaskList_empty_doesNothing() {
        Board board = new Board();
        board.addTaskList(List.of());
        assertTrue(board.getTaskList().isEmpty());
    }

    @Test
    void testAddTaskList_addsAndSetsBackReferences() {
        Board board = new Board();
        Task t1 = new Task();
        Task t2 = new Task();
        List<Task> list = new ArrayList<>();
        list.add(t1);
        list.add(t2);
        board.addTaskList(list);
        assertEquals(2, board.getTaskList().size());
        assertSame(board, t1.getBoard());
        assertSame(board, t2.getBoard());
    }

    @Test
    void testRemoveTaskList_removesWhenPresent() {
        Board board = new Board();
        Task t1 = new Task();
        Task t2 = new Task();
        Task t3 = new Task();
        board.addTaskList(List.of(t1, t2, t3));
        board.removeTaskList(List.of(t2));
        assertEquals(2, board.getTaskList().size());
        assertTrue(board.getTaskList().contains(t1));
        assertTrue(board.getTaskList().contains(t3));
        assertFalse(board.getTaskList().contains(t2));
    }

    @Test
    void testClearTaskList_whenNullInitializesAndReturns() {
        Board board = new Board();
        board.setTaskList(null);
        board.clearTaskList();
        assertNotNull(board.getTaskList());
        assertTrue(board.getTaskList().isEmpty());
    }

    @Test
    void testClearTaskList_whenHasElements_clearsAll() {
        Board board = new Board();
        Task t = new Task("n", "d", TaskStatus.NONE, TaskIcon.NONE);
        board.addTask(t);
        assertFalse(board.getTaskList().isEmpty());
        board.clearTaskList();
        assertTrue(board.getTaskList().isEmpty());
    }
}
