// src/test/java/com/example/task_board_be/pojo/entity/BaseEntityTest.java
package com.example.task_board_be.pojo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void testEquals_sameReference_true() {
        Board a = new Board();
        a.setId(1L);
        assertEquals(a, a);
    }

    @Test
    void testEquals_nullId_returnsFalse() {
        Board a = new Board();
        Board b = new Board();
        assertNotEquals(a, b);
    }

    @Test
    void testEquals_differentIds_false() {
        Board a = new Board();
        a.setId(1L);
        Board b = new Board();
        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void testEquals_sameId_true() {
        Board a = new Board();
        a.setId(5L);
        Board b = new Board();
        b.setId(5L);
        assertEquals(a, b);
    }

    @Test
    void testHashCode_nullId_usesIdentity() {
        Board a = new Board();
        Board b = new Board();
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHashCode_sameId_sameHash() {
        Board a = new Board();
        a.setId(10L);
        Board b = new Board();
        b.setId(10L);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testArchivedAndTimestamps_accessors() {
        Task t = new Task();
        assertFalse(t.isArchived());
        t.setIsArchived(true);
        assertTrue(t.isArchived());
        t.setCreatedAt(java.time.LocalDateTime.now());
        t.setUpdatedAt(java.time.LocalDateTime.now());
        assertNotNull(t.getCreatedAt());
        assertNotNull(t.getUpdatedAt());
        assertTrue(t.toString().contains("BaseEntity"));
    }
}
