package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.exception.ExternalServiceException;
import com.sbprojects.storm_shield.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testHandleExternalServiceException_SanitizesResponse(){
        //1. Arrange
        String internalTrace = "NullPointerException: line 84 inside closed dependency jar file";
        ExternalServiceException exception = new ExternalServiceException(internalTrace);

        //2. Act
        ResponseEntity<Map<String, Object>> response = handler.handleExternalServiceException(exception);

        //3. Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Map<String, Object> body = response.getBody();

        //Verify that the sensitive system trace message is blocked from the public payload
        assertFalse(body.get("message").toString().contains("NullPointerException"));
        assertEquals("Upstream weather providers are currently unresponsive. Operations are failing over to fallback parameters.", body.get("message"));


    }
}
