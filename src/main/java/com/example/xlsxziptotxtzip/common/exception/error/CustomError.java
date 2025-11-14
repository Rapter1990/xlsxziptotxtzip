package com.example.xlsxziptotxtzip.common.exception.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error payload for API responses.
 * Used by global exception handlers to return a consistent structure
 * (status, message, and optional field-level sub-errors) on failures.
 */
@Getter
@Builder
public class CustomError {

    @Builder.Default
    private LocalDateTime time = LocalDateTime.now();

    private HttpStatus httpStatus;

    private String header;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @Builder.Default
    private final Boolean isSuccess = false;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CustomSubError> subErrors;

    /**
     * Represents a sub-error, usually used for validation problems.
     */
    @Getter
    @Builder
    public static class CustomSubError {

        private String message;

        private String field;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object value;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String type;

    }

    /**
     * Enumeration of predefined error categories for consistent API responses.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Header {

        API_ERROR("API ERROR"),

        VALIDATION_ERROR("VALIDATION ERROR"),

        PROCESS_ERROR("PROCESS ERROR");

        private final String name;

    }

}
