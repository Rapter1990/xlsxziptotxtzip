package com.example.xlsxziptotxtzip.common.exception;

import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all custom API exceptions in the application.
 * Subclasses must define the associated {@link HttpStatus} and
 * {@link CustomError.Header} so that the global exception handler
 * can map them to a consistent {@link CustomError} response.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    /**
     * Creates a new API exception with a human-readable message.
     *
     * @param message explanation for clients and logs
     */
    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();

    public abstract CustomError.Header getHeader();

}

