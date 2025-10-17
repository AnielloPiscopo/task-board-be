package com.example.task_board_be.pojo.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseModelTest {

    @Test
    void testDefaultConstructorAndAccessors() {
        BaseModel model = new BaseModel() {}; // classe anonima concreta

        model.setId(10L);
        model.setIsArchived(true);
        LocalDateTime now = LocalDateTime.now();
        model.setCreatedAt(now);
        model.setUpdatedAt(now);

        assertEquals(10L, model.getId());
        assertTrue(model.isArchived());
        assertEquals(now, model.getCreatedAt());
        assertEquals(now, model.getUpdatedAt());
        assertTrue(model.toString().contains("BaseModel"));
    }

    @Test
    void testConstructorWithId() {
        BaseModel model = new BaseModel(5L) {};
        assertEquals(5L, model.getId());
    }
}
