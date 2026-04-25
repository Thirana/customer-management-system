package com.example.customermanagement.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.customermanagement.dto.response.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApplicationExceptionReturnsConfiguredStatusAndMessage() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleApplicationException(new DuplicateNicException());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("A customer already exists with the provided NIC number.", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidReturnsFieldErrors() throws NoSuchMethodException {
        SampleRequest target = new SampleRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "sampleRequest");
        bindingResult.addError(new FieldError("sampleRequest", "name", "must not be blank"));

        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleEndpoint", SampleRequest.class),
                0
        );
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response =
                handler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed.", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getData().get("name"));
    }

    @SuppressWarnings("unused")
    private void sampleEndpoint(SampleRequest request) {
    }

    private static class SampleRequest {
    }
}
