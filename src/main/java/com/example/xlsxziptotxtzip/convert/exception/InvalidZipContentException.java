package com.example.xlsxziptotxtzip.convert.exception;

import com.example.xlsxziptotxtzip.common.exception.ApiException;
import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the uploaded ZIP content is invalid
 */
public class InvalidZipContentException extends ApiException {

    public static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    public static final CustomError.Header HEADER = CustomError.Header.API_ERROR;

    public InvalidZipContentException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return STATUS;
    }

    @Override
    public CustomError.Header getHeader() {
        return HEADER;
    }
}
