package com.example.task_board_be.exception;

import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.pojo.resource.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest mockReq(String method, String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod(method);
        req.setRequestURI(uri);
        return req;
    }

    @Test
    void testHandleNotFound_returns404() {
        HttpServletRequest req = mockReq("GET", "/boards/1");
        NotFoundException ex = new NotFoundException("board", 1L);
        ResponseEntity<ErrorResource> resp = handler.handleNotFound(ex, req);
        assertEquals(404, resp.getStatusCode().value());
        assertEquals(404, resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().contains("board not found: id=1"));
    }

    @Test
    void testHandleBadRequest_returns400() {
        HttpServletRequest req = mockReq("POST", "/boards");
        org.apache.coyote.BadRequestException ex = new org.apache.coyote.BadRequestException("bad req");
        ResponseEntity<ErrorResource> resp = handler.handleBadRequest(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertEquals("bad req", resp.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArg_returns400() {
        HttpServletRequest req = mockReq("POST", "/boards");
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<ErrorResource> resp = handler.handleIllegalArg(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertEquals("bad arg", resp.getBody().getMessage());
    }

    @Test
    void testHandleMethodNotSupported_returns405_withAllowHeader() {
        HttpServletRequest req = mockReq("POST", "/boards");
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("POST", java.util.List.of("GET", "PUT")) {
                    @Override
                    public Set<HttpMethod> getSupportedHttpMethods() {
                        return Set.of(HttpMethod.GET, HttpMethod.PUT);
                    }
                };
        ResponseEntity<ErrorResource> resp = handler.handleMethodNotSupported(ex, req);
        assertEquals(405, resp.getStatusCode().value());
        assertTrue(resp.getHeaders().getAllow().contains(HttpMethod.GET));
        assertTrue(resp.getBody().getMessage().contains("Request method 'POST' is not supported"));
    }

    @Test
    void testHandleMethodNotSupported_returns405_noAllow() {
        HttpServletRequest req = mockReq("PATCH", "/boards");
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<ErrorResource> resp = handler.handleMethodNotSupported(ex, req);
        assertEquals(405, resp.getStatusCode().value());
        assertNotNull(resp.getHeaders().getAllow());
        assertTrue(resp.getBody().getMessage().contains("PATCH"));
    }

    @Test
    void testHandleNoHandler_returns404() {
        HttpServletRequest req = mockReq("GET", "/missing");
        NoHandlerFoundException ex =
                new NoHandlerFoundException("GET", "/missing", new HttpHeaders());
        ResponseEntity<ErrorResource> resp = handler.handleNoHandler(ex, req);
        assertEquals(404, resp.getStatusCode().value());
        assertTrue(resp.getBody().getMessage().contains("No handler for GET /missing"));
    }

    @Test
    void testHandleMethodArgumentNotValid_returns400() throws Exception {
        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "obj");
        br.addError(new FieldError("obj", "fieldA", "must not be null"));
        class Dummy {
            void m(String p) {
            }
        }
        Method m = Dummy.class.getDeclaredMethod("m", String.class);
        MethodParameter mp = new MethodParameter(m, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);
        HttpServletRequest req = mockReq("POST", "/boards");
        ResponseEntity<ErrorResource> resp = handler.handleMethodArgumentNotValid(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().startsWith("Validation failed: "));
    }

    @Test
    void testHandleConstraintViolation_returns400() {
        @SuppressWarnings("unchecked")
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("id");
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("must be positive");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));
        HttpServletRequest req = mockReq("GET", "/boards?id=-1");
        ResponseEntity<ErrorResource> resp = handler.handleConstraintViolation(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertEquals("Constraint violation", resp.getBody().getMessage());
    }

    @Test
    void testHandleNotReadable_returns400() {
        RuntimeException cause = new RuntimeException("bad json");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("boom", cause);
        HttpServletRequest req = mockReq("POST", "/tasks");
        ResponseEntity<ErrorResource> resp = handler.handleNotReadable(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertTrue(resp.getBody().getMessage().contains("Malformed JSON request"));
    }

    @Test
    void testHandleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);
        HttpServletRequest req = mockReq("GET", "/tasks/abc");
        ResponseEntity<ErrorResource> resp = handler.handleTypeMismatch(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertTrue(resp.getBody().getMessage().contains("Parameter 'id' invalid: expected Long"));
    }

    @Test
    void testHandleBind_returns400() {
        Object target = new Object();
        BindException ex = new BindException(target, "obj");
        ex.addError(new FieldError("obj", "fieldB", "must be present"));
        HttpServletRequest req = mockReq("GET", "/search");
        ResponseEntity<ErrorResource> resp = handler.handleBind(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertEquals("Bind failed", resp.getBody().getMessage());
    }

    @Test
    void testHandleDataIntegrity_returns409() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("duplicate key", new RuntimeException("fk violation"));
        HttpServletRequest req = mockReq("POST", "/boards");
        ResponseEntity<ErrorResource> resp = handler.handleDataIntegrity(ex, req);
        assertEquals(409, resp.getStatusCode().value());
        assertEquals(409, resp.getBody().getStatus());
        assertEquals("Data integrity violation", resp.getBody().getMessage());
    }

    @Test
    void testHandleMissingRequestParam_returns400() {
        HttpServletRequest req = mockReq("GET", "/tasks");
        org.springframework.web.bind.MissingServletRequestParameterException ex =
                new org.springframework.web.bind.MissingServletRequestParameterException("boardId", "Long");
        ResponseEntity<ErrorResource> resp = handler.handleMissingRequestParam(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().contains("Required request parameter 'boardId'"));
    }

    @Test
    void testHandleGeneric_returns500() {
        Exception ex = new Exception("boom");
        HttpServletRequest req = mockReq("GET", "/any");
        ResponseEntity<ErrorResource> resp = handler.handleGeneric(ex, req);
        assertEquals(500, resp.getStatusCode().value());
        assertEquals(500, resp.getBody().getStatus());
        assertEquals("boom", resp.getBody().getMessage());
    }

    @Test
    void testHandleBadRequest_nullMessage_usesDefault() {
        HttpServletRequest req = mockReq("POST", "/boards");
        org.apache.coyote.BadRequestException ex = new org.apache.coyote.BadRequestException();
        ResponseEntity<ErrorResource> resp = handler.handleBadRequest(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Bad request", resp.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArg_blankMessage_usesDefault() {
        HttpServletRequest req = mockReq("GET", "/x");
        IllegalArgumentException ex = new IllegalArgumentException("   ");
        ResponseEntity<ErrorResource> resp = handler.handleIllegalArg(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Bad request", resp.getBody().getMessage());
    }

    @Test
    void testHandleNotReadable_nullCause_path() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("boom");
        HttpServletRequest req = mockReq("POST", "/tasks");
        ResponseEntity<ErrorResource> resp = handler.handleNotReadable(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Malformed JSON request", resp.getBody().getMessage());
    }

    @Test
    void testHandleTypeMismatch_nullRequiredType_usesQuestionMark() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", null, "id", null, null);
        HttpServletRequest req = mockReq("GET", "/tasks/abc");
        ResponseEntity<ErrorResource> resp = handler.handleTypeMismatch(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        assertTrue(resp.getBody().getMessage().contains("expected ?"));
    }

    @Test
    void testHandleDataIntegrity_nullMostSpecificCause_path() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("dup");
        HttpServletRequest req = mockReq("POST", "/boards");
        ResponseEntity<ErrorResource> resp = handler.handleDataIntegrity(ex, req);
        assertEquals(409, resp.getStatusCode().value());
        assertEquals("Data integrity violation", resp.getBody().getMessage());
    }

}
