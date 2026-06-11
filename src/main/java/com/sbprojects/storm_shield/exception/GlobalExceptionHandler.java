package com.sbprojects.storm_shield.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
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
}
