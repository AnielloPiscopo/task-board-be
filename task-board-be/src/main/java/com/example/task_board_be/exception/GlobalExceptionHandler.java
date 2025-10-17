package com.example.task_board_be.exception;

import com.example.task_board_be.exception.custom.NotFoundException;
import com.example.task_board_be.pojo.resource.ErrorResource;
import com.example.task_board_be.utils.LoggerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* 404 dominio (entity non trovata) */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResource> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        logger.warn("ECCEZZIONE STATUS 404");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResource(404, ex.getMessage()));
    }

    /* 400 bad request esplicita dal dominio */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResource> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Bad request";

        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), msg);

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Bad request";
        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), msg);
        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResource> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest req) {
        Set<HttpMethod> methods = ex.getSupportedHttpMethods();
        Set<String> allowedNames = (methods == null)
                ? Collections.emptySet()
                : methods.stream()
                .map(HttpMethod::name)              // <-- qui il fix
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String msg = "Request method '" + ex.getMethod() + "' is not supported. Allowed: " + allowedNames;

        logger.warn("ECCEZZIONE STATUS 405");
        logger.warn("{} - [URI:{} ; METHOD:{} ; ALLOWED:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), allowedNames, msg);

        HttpMethod[] allowArray = (methods == null) ? new HttpMethod[0] : methods.toArray(new HttpMethod[0]);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .allow(allowArray)                          // header Allow
                .body(new ErrorResource(405, msg));
    }

    /* 404 nessun handler per path (serve spring.mvc.throw-exception-if-no-handler-found=true) */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResource> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        String msg = "No handler for " + ex.getHttpMethod() + " " + ex.getRequestURL();

        logger.warn("ECCEZZIONE STATUS 404");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), msg);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResource(404, msg));
    }

    /* 400 validazione @Valid sul body */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest req) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .distinct()
                .toList();

        String msg = "Validation failed: " + String.join("; ", errors);

        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; ERRORS:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), errors);

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }


    /* 400 validazione su @RequestParam / @PathVariable / @Validated (non-body) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResource> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        String msg = "Constraint violation";
        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; ERRORS:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(),
                ex.getConstraintViolations().stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .distinct().toList());

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    /* 400 body non leggibile / JSON malformato */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResource> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest req) {
        String msg = "Malformed JSON request";

        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    /* 400 tipo sbagliato nel path/param (es. id non numerico) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResource> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest req) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";
        String msg = "Parameter '" + ex.getName() + "' invalid: expected " + expected;

        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(), msg);

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    /* 400 binding error su @ModelAttribute / query param complessi */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResource> handleBind(BindException ex, HttpServletRequest req) {
        String msg = "Bind failed";

        logger.warn("ECCEZZIONE STATUS 400");
        logger.warn("{} - [URI:{} ; METHOD:{} ; ERRORS:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(),
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .distinct().toList());

        return ResponseEntity.badRequest().body(new ErrorResource(400, msg));
    }

    /* 409 vincoli DB */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResource> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        String msg = "Data integrity violation";

        logger.warn("ECCEZZIONE STATUS 409");
        logger.warn("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exception", true),
                req.getRequestURI(), req.getMethod(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResource(409, msg));
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public org.springframework.http.ResponseEntity<com.example.task_board_be.pojo.resource.ErrorResource> handleMissingRequestParam(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            jakarta.servlet.http.HttpServletRequest req) {

        String msg = ex.getMessage();
        return org.springframework.http.ResponseEntity
                .badRequest()
                .body(new com.example.task_board_be.pojo.resource.ErrorResource(400, msg));
    }


    /* 500 catch-all */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleGeneric(Exception ex, HttpServletRequest req) {
        logger.error("ECCEZZIONE STATUS 500");
        logger.error("{} - [URI:{} ; METHOD:{} ; MSG:{}]",
                LoggerUtils.getStandardLoggerMsg("exc", true),
                req.getRequestURI(), req.getMethod(), ex.getMessage(), ex);

        // in prod, meglio un messaggio generico; per ora ti restituisco ex.getMessage() come avevi
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResource(500, ex.getMessage()));
    }
}
