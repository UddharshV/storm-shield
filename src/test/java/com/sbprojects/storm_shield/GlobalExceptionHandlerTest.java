package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.exception.ExternalServiceException;
import com.sbprojects.storm_shield.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    @Test
    public void testHandleValidationException_ProducesCleanBadRequestBody() {
        // 1. Arrange: simulate a failed validation on a DTO with a bad "status" field
        Object targetObject = new Object(); // target can be any object for this unit test
        BindingResult bindingResult = new BeanPropertyBindingResult(targetObject, "routeStatusUpdateRequest");

        // Add one field error as if @NotBlank on "status" failed
        bindingResult.addError(new FieldError(
                "routeStatusUpdateRequest", // objectName
                "status",                   // field
                "",                         // rejectedValue
                false,                      // bindingFailure
                null,                       // codes
                null,                       // arguments
                "Status must not be blank"  // defaultMessage
        ));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        // 2. Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex);

        // 3. Assert: top-level HTTP status and basic structure
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("One or more fields failed validation.", body.get("message"));

        // 4. Assert: fieldErrors contents
        @SuppressWarnings("unchecked")
        var fieldErrors = (java.util.List<Map<String, String>>) body.get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals(1, fieldErrors.size());

        Map<String, String> fieldError = fieldErrors.get(0);
        assertEquals("status", fieldError.get("field"));
        assertEquals("Status must not be blank", fieldError.get("message"));
    }
}
