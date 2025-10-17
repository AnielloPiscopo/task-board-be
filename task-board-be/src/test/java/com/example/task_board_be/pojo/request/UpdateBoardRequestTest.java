package com.example.task_board_be.pojo.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateBoardRequestTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    void testGettersSettersAndToString() {
        UpdateBoardRequest req = new UpdateBoardRequest();
        req.setName("Rename");
        req.setDescription("Updated");
        assertEquals("Rename", req.getName());
        assertEquals("Updated", req.getDescription());
        assertTrue(req.toString().contains("UpdateBoardRequest"));
    }

    @Test
    void testDescriptionSizeConstraint() {
        String tooLongDesc = "y".repeat(256);
        UpdateBoardRequest req = new UpdateBoardRequest("X", tooLongDesc);
        Set<ConstraintViolation<UpdateBoardRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "description".equals(cv.getPropertyPath().toString())));
    }
}
