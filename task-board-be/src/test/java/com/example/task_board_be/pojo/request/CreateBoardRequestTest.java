package com.example.task_board_be.pojo.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintViolation;

class CreateBoardRequestTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    void testGettersSettersAndToString() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setName("New board");
        req.setDescription("desc");
        assertEquals("New board", req.getName());
        assertEquals("desc", req.getDescription());
        assertTrue(req.toString().contains("CreateBoardRequest"));
    }

    @Test
    void testSizeConstraints() {
        String tooLongName = "x".repeat(201);
        String tooLongDesc = "y".repeat(256);
        CreateBoardRequest req = new CreateBoardRequest(tooLongName, tooLongDesc);
        Set<ConstraintViolation<CreateBoardRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "name".equals(cv.getPropertyPath().toString())));
        assertTrue(v.stream().anyMatch(cv -> "description".equals(cv.getPropertyPath().toString())));
    }
}
