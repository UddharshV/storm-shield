package com.sbprojects.storm_shield.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice //Registers this class as the central safety net for application exceptions
public class GlobalExceptionHandler {

    //Catch and resolve our specific external service connection dropouts
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalServiceException(ExternalServiceException ex){
        //1.Internal Logging
        System.err.println("[INTERNAL LOG] - SEVERE: Outbound Dependency Failure: " + ex.getMessage());

        //2. Sanitized Response
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.BAD_GATEWAY.value());
        errorBody.put("error", "Bad Gateway");
        errorBody.put("message", "Upstream weather providers are currently unresponsive. Operations are failing over to fallback parameters.");

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_GATEWAY);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // Avoid ex.getMessage() because it can blow up when MethodParameter is null in tests
        System.err.println("[INTERNAL LOG] - WARN: VALIDATION ERROR on object: "
                + ex.getBindingResult().getObjectName());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", "One or more fields failed validation.");

        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("field", fieldError.getField());
                    m.put("message", fieldError.getDefaultMessage());
                    return m;
                })
                .toList();

        errorBody.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }
}
