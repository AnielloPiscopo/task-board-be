package com.example.task_board_be.pojo.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTaskRequestTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    void testGettersSettersAndToString() {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setName("Rename");
        req.setDescription("New desc");
        req.setStatus("TODO");
        req.setIcon("FEATURE");
        assertEquals("Rename", req.getName());
        assertEquals("New desc", req.getDescription());
        assertEquals("TODO", req.getStatus());
        assertEquals("FEATURE", req.getIcon());
        assertTrue(req.toString().contains("UpdateTaskRequest"));
    }

    @Test
    void testDescriptionSizeConstraint() {
        String tooLongDesc = "y".repeat(256);
        UpdateTaskRequest req = new UpdateTaskRequest("A", tooLongDesc, "TODO", "BUG");
        Set<ConstraintViolation<UpdateTaskRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "description".equals(cv.getPropertyPath().toString())));
    }
}
