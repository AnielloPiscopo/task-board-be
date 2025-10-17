package com.example.task_board_be.pojo.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateTaskRequestTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    void testGettersSettersAndToString() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setName("New task");
        req.setDescription("desc");
        req.setStatus("TODO");
        req.setIcon("BUG");
        req.setBoardId(3L);
        assertEquals("New task", req.getName());
        assertEquals("desc", req.getDescription());
        assertEquals("TODO", req.getStatus());
        assertEquals("BUG", req.getIcon());
        assertEquals(3L, req.getBoardId());
        assertTrue(req.toString().contains("CreateTaskRequest"));
    }

    @Test
    void testBoardIdNotNullConstraint() {
        CreateTaskRequest req = new CreateTaskRequest("a", "b", "TODO", "BUG", null);
        Set<ConstraintViolation<CreateTaskRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "boardId".equals(cv.getPropertyPath().toString())));
    }

    @Test
    void testSizeConstraints() {
        String tooLongName = "x".repeat(201);
        String tooLongDesc = "y".repeat(256);
        CreateTaskRequest req = new CreateTaskRequest(tooLongName, tooLongDesc, "TODO", "BUG", 1L);
        Set<ConstraintViolation<CreateTaskRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "name".equals(cv.getPropertyPath().toString())));
        assertTrue(v.stream().anyMatch(cv -> "description".equals(cv.getPropertyPath().toString())));
    }
}
