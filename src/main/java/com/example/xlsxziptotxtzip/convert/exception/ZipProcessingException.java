package com.example.xlsxziptotxtzip.convert.exception;

import com.example.xlsxziptotxtzip.common.exception.ApiException;
import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

/**
 * Exception representing a failure while processing a ZIP archive
 */
public class ZipProcessingException extends ApiException {

    public static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    public static final CustomError.Header HEADER = CustomError.Header.PROCESS_ERROR;

    public ZipProcessingException(String reason, Throwable cause) {
        super("Failed to process ZIP: " + reason);
        initCause(cause);
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
