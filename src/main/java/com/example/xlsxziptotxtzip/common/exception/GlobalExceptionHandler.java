package com.example.xlsxziptotxtzip.common.exception;

import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST controllers.
 * Centralizes handling of common framework and custom exceptions and converts them
 * into a consistent {@link CustomError} response body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors on request bodies annotated with {@code @Valid}.
     * Converts {@link MethodArgumentNotValidException} into a {@link CustomError}
     * with {@link CustomError.Header#VALIDATION_ERROR} and detailed
     * {@link CustomError.CustomSubError} entries for each invalid field.
     *
     * @param ex the thrown {@link MethodArgumentNotValidException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {

        List<CustomError.CustomSubError> subErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(
                error -> {
                    String fieldName = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
                    String message = error.getDefaultMessage();
                    subErrors.add(
                            CustomError.CustomSubError.builder()
                                    .field(fieldName)
                                    .message(message)
                                    .build()
                    );
                }
        );

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Validation failed")
                .subErrors(subErrors)
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violations on path variables or request parameters.
     * Transforms {@link ConstraintViolationException} into a {@link CustomError}
     * with {@link CustomError.Header#VALIDATION_ERROR} and one
     * {@link CustomError.CustomSubError} per violated constraint.
     *
     * @param ex the thrown {@link ConstraintViolationException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 400
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handlePathVariableErrors(final ConstraintViolationException ex) {

        List<CustomError.CustomSubError> subErrors = new ArrayList<>();
        ex.getConstraintViolations()
                .forEach(constraintViolation -> {
                            Object invalidValue = constraintViolation.getInvalidValue();
                            String propertyPath = constraintViolation.getPropertyPath().toString();
                            String fieldName = propertyPath.contains(".")
                                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                                    : propertyPath;

                            subErrors.add(
                                    CustomError.CustomSubError.builder()
                                            .message(constraintViolation.getMessage())
                                            .field(fieldName)
                                            .value(invalidValue != null ? invalidValue.toString() : null)
                                            .type(invalidValue != null ? invalidValue.getClass().getSimpleName() : null)
                                            .build()
                            );
                        }
                );

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Constraint violation")
                .subErrors(subErrors)
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing multipart parts (e.g. missing {@code file} part in a multipart request).
     * Converts {@link MissingServletRequestPartException} into a {@link CustomError}
     * with {@link CustomError.Header#API_ERROR}.
     *
     * @param ex the thrown {@link MissingServletRequestPartException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 400
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<Object> handleMissingPart(final MissingServletRequestPartException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Required request part is missing: " + ex.getRequestPartName())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles file upload size limit violations.
     * Converts {@link MaxUploadSizeExceededException} into a {@link CustomError}
     * with {@link CustomError.Header#API_ERROR} and status 413.
     *
     * @param ex the thrown {@link MaxUploadSizeExceededException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.PAYLOAD_TOO_LARGE)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Uploaded file is too large")
                .build();

        return new ResponseEntity<>(customError, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handles malformed JSON or unreadable request bodies.
     * Converts {@link HttpMessageNotReadableException} into a {@link CustomError}
     * with {@link CustomError.Header#API_ERROR}.
     *
     * @param ex the thrown {@link HttpMessageNotReadableException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Malformed request body")
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles argument type mismatches (e.g., invalid path variable or request parameter type).
     * Converts {@link MethodArgumentTypeMismatchException} into a {@link CustomError}
     * with {@link CustomError.Header#API_ERROR}.
     *
     * @param ex the thrown {@link MethodArgumentTypeMismatchException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Invalid value for parameter: " + ex.getName())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic {@link IllegalStateException} from services or utilities
     * (for example, XLSX conversion failures).
     * Converts the exception into a {@link CustomError} with
     * {@link CustomError.Header#PROCESS_ERROR}.
     *
     * @param ex the thrown {@link IllegalStateException}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 500
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalState(final IllegalStateException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Fallback handler for any unhandled exceptions.
     * Acts as a safety net for unexpected errors, converting them into a generic
     * {@link CustomError} with {@link CustomError.Header#API_ERROR}.
     *
     * @param ex the thrown {@link Exception}
     * @return a {@link ResponseEntity} containing a {@link CustomError} and HTTP 500
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGeneralException(final Exception ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.API_ERROR.getName())
                .message("Unexpected error occurred")
                .build();

        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all custom {@link ApiException} subclasses.
     * Uses {@link ApiException#getStatus()} and {@link ApiException#getHeader()}
     * to build a corresponding {@link CustomError} response.
     *
     * @param ex the thrown {@link ApiException}
     * @return a {@link ResponseEntity} containing a {@link CustomError}
     * and the status provided by {@link ApiException#getStatus()}
     */
    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<Object> handleApiException(final ApiException ex) {

        CustomError customError = CustomError.builder()
                .httpStatus(ex.getStatus())
                .header(ex.getHeader().getName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, ex.getStatus());
    }

}
