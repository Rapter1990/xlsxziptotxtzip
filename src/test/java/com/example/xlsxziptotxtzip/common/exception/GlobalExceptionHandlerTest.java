package com.example.xlsxziptotxtzip.common.exception;

import com.example.xlsxziptotxtzip.base.AbstractBaseServiceTest;
import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest extends AbstractBaseServiceTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleMethodArgumentNotValid_returnsBadRequest_withSubErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        FieldError fe = new FieldError("obj", "age", "must be >= 18");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fe));

        ResponseEntity<Object> resp = globalExceptionHandler.handleMethodArgumentNotValid(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Validation failed")
                .subErrors(List.of(CustomError.CustomSubError.builder()
                        .field("age")
                        .message("must be >= 18")
                        .build()))
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleConstraintViolation_returnsBadRequest_withMappedViolations() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);

        when(cv.getMessage()).thenReturn("must be positive");
        when(cv.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("dto.amount");
        when(cv.getInvalidValue()).thenReturn(-5);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));

        ResponseEntity<Object> resp = globalExceptionHandler.handlePathVariableErrors(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Constraint violation")
                .subErrors(List.of(CustomError.CustomSubError.builder()
                        .message("must be positive")
                        .field("amount")
                        .value("-5")
                        .type("Integer")
                        .build()))
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleConstraintViolation_whenPathHasNoDot_andInvalidValueIsNull_setsFieldAndNullValueAndType() {
        // given
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);

        when(cv.getMessage()).thenReturn("must not be blank");
        when(cv.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("username");
        when(cv.getInvalidValue()).thenReturn(null);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));

        // when
        ResponseEntity<Object> resp = globalExceptionHandler.handlePathVariableErrors(ex);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Constraint violation")
                .subErrors(List.of(
                        CustomError.CustomSubError.builder()
                                .message("must not be blank")
                                .field("username")
                                .value(null)
                                .type(null)
                                .build()
                ))
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleMissingPart_returnsBadRequest_withApiErrorHeader() {

        MissingServletRequestPartException ex = new MissingServletRequestPartException("file");

        ResponseEntity<Object> resp = globalExceptionHandler.handleMissingPart(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Required request part is missing: file")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleMaxUploadSizeExceeded_returnsPayloadTooLarge_withApiErrorHeader() {

        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024L);

        ResponseEntity<Object> resp = globalExceptionHandler.handleMaxUploadSizeExceeded(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.PAYLOAD_TOO_LARGE)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Uploaded file is too large")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleHttpMessageNotReadable_returnsBadRequest_withApiErrorHeader() {

        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("invalid json", inputMessage);

        ResponseEntity<Object> resp = globalExceptionHandler.handleHttpMessageNotReadable(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Malformed request body")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleTypeMismatch_returnsBadRequest_withParameterName() {

        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName()).thenReturn("id");

        ResponseEntity<Object> resp = globalExceptionHandler.handleTypeMismatch(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Invalid value for parameter: id")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleIllegalState_returnsInternalServerError_withProcessErrorHeader() {
        IllegalStateException ex = new IllegalStateException("XLSX conversion failed");

        ResponseEntity<Object> resp = globalExceptionHandler.handleIllegalState(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .message("XLSX conversion failed")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleGeneralException_returnsInternalServerError_withGenericMessage() {
        Exception ex = new Exception("boom");

        ResponseEntity<Object> resp = globalExceptionHandler.handleGeneralException(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Unexpected error occurred")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    @Test
    void handleApiException_returnsMappedStatusAndHeader() {
        ApiException ex = mock(ApiException.class);
        when(ex.getStatus()).thenReturn(HttpStatus.UNPROCESSABLE_ENTITY);
        when(ex.getHeader()).thenReturn(CustomError.Header.PROCESS_ERROR);
        when(ex.getMessage()).thenReturn("Custom api error");

        ResponseEntity<Object> resp = globalExceptionHandler.handleApiException(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .message("Custom api error")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    private void checkCustomError(CustomError expectedError, CustomError actualError) {

        assertThat(actualError).isNotNull();
        assertThat(actualError.getTime()).isNotNull();
        assertThat(actualError.getHeader()).isEqualTo(expectedError.getHeader());
        assertThat(actualError.getIsSuccess()).isEqualTo(expectedError.getIsSuccess());

        if (expectedError.getMessage() != null) {
            assertThat(actualError.getMessage()).isEqualTo(expectedError.getMessage());
        }

        if (expectedError.getSubErrors() != null) {
            assertThat(actualError.getSubErrors().size()).isEqualTo(expectedError.getSubErrors().size());
            if (!expectedError.getSubErrors().isEmpty()) {
                assertThat(actualError.getSubErrors().getFirst().getMessage()).isEqualTo(expectedError.getSubErrors().get(0).getMessage());
                assertThat(actualError.getSubErrors().getFirst().getField()).isEqualTo(expectedError.getSubErrors().get(0).getField());
                assertThat(actualError.getSubErrors().getFirst().getValue()).isEqualTo(expectedError.getSubErrors().get(0).getValue());
                assertThat(actualError.getSubErrors().getFirst().getType()).isEqualTo(expectedError.getSubErrors().get(0).getType());
            }
        }

    }



}