package com.example.task_board_be.pojo.model;

import com.example.task_board_be.enums.task.TaskIcon;
import com.example.task_board_be.enums.task.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskModelTest {

    @Test
    void testDefaultConstructorAndAccessors() {
        TaskModel model = new TaskModel();
        model.setId(1L);
        model.setName("Fix login bug");
        model.setDescription("Resolve NPE on login");
        model.setStatus(TaskStatus.NONE);
        model.setIcon(TaskIcon.BUG);

        BoardModel board = new BoardModel();
        board.setId(100L);
        model.setBoardModel(board);

        assertEquals(1L, model.getId());
        assertEquals("Fix login bug", model.getName());
        assertEquals("Resolve NPE on login", model.getDescription());
        assertEquals(TaskStatus.NONE, model.getStatus());
        assertEquals(TaskIcon.BUG, model.getIcon());
        assertEquals(100L, model.getBoardModel().getId());
    }

    @Test
    void testConstructorWithNameDescStatusIcon() {
        TaskModel model = new TaskModel("Feature A", "Add search", TaskStatus.NONE, TaskIcon.FEATURE);
        assertEquals("Feature A", model.getName());
        assertEquals("Add search", model.getDescription());
        assertEquals(TaskStatus.NONE, model.getStatus());
        assertEquals(TaskIcon.FEATURE, model.getIcon());
    }

    @Test
    void testConstructorWithIdNameDescStatusIcon() {
        TaskModel model = new TaskModel(5L, "Refactor", "Cleanup code", TaskStatus.NONE, TaskIcon.REFACTOR);
        assertEquals(5L, model.getId());
        assertEquals("Refactor", model.getName());
        assertEquals("Cleanup code", model.getDescription());
        assertEquals(TaskStatus.NONE, model.getStatus());
        assertEquals(TaskIcon.REFACTOR, model.getIcon());
    }

    @Test
    void testToString_containsFields() {
        TaskModel model = new TaskModel("X", "Y", TaskStatus.NONE, TaskIcon.TEST);
        String out = model.toString();
        assertTrue(out.contains("TaskModel"));
        assertTrue(out.contains("status"));
    }
}
