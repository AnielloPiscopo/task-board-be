package com.example.task_board_be.pojo.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardModelTest {

    @Test
    void testDefaultConstructorAndAccessors() {
        BoardModel model = new BoardModel();
        model.setId(1L);
        model.setName("Development");
        model.setDescription("Main board");

        assertEquals(1L, model.getId());
        assertEquals("Development", model.getName());
        assertEquals("Main board", model.getDescription());
        assertNotNull(model.getTaskModelList());
        assertTrue(model.getTaskModelList().isEmpty());
    }

    @Test
    void testConstructorWithNameDescription() {
        BoardModel model = new BoardModel("Design", "UI tasks");
        assertEquals("Design", model.getName());
        assertEquals("UI tasks", model.getDescription());
    }

    @Test
    void testConstructorWithIdNameDescription() {
        BoardModel model = new BoardModel(10L, "Testing", "QA tasks");
        assertEquals(10L, model.getId());
        assertEquals("Testing", model.getName());
        assertEquals("QA tasks", model.getDescription());
    }

    @Test
    void testTaskModelListSetterGetter() {
        BoardModel model = new BoardModel();
        TaskModel t1 = new TaskModel();
        TaskModel t2 = new TaskModel();
        model.setTaskModelList(List.of(t1, t2));

        assertEquals(2, model.getTaskModelList().size());
        assertTrue(model.getTaskModelList().contains(t1));
        assertTrue(model.toString().contains("BoardModel"));
    }
}
