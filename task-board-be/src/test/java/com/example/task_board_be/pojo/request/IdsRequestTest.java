package com.example.task_board_be.pojo.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdsRequestTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    void testGettersSettersAndToString() {
        IdsRequest req = new IdsRequest();
        req.setIdList(List.of(1L, 2L, 3L));
        assertEquals(List.of(1L, 2L, 3L), req.getIdList());
        assertTrue(req.toString().contains("IdsRequest"));
    }

    @Test
    void testNotEmptyConstraint_whenNull() {
        IdsRequest req = new IdsRequest();
        req.setIdList(null);
        Set<ConstraintViolation<IdsRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "idList".equals(cv.getPropertyPath().toString())));
    }

    @Test
    void testNotEmptyConstraint_whenEmpty() {
        IdsRequest req = new IdsRequest(List.of());
        Set<ConstraintViolation<IdsRequest>> v = validator.validate(req);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(cv -> "idList".equals(cv.getPropertyPath().toString())));
    }

    @Test
    void testElementConstraint_nullElement() {
        IdsRequest req = new IdsRequest(java.util.Arrays.asList(1L, null));
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testElementConstraint_negativeElement() {
        IdsRequest req = new IdsRequest(java.util.List.of(-5L));
        assertFalse(validator.validate(req).isEmpty());
    }

}
