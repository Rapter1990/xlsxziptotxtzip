package com.example.xlsxziptotxtzip.convert.exception;

import com.example.xlsxziptotxtzip.common.exception.ApiException;
import com.example.xlsxziptotxtzip.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

/**
 * Exception indicating a failure during XLSX → TXT conversion.
 */
public class XlsxConversionException extends ApiException {

    public static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    public static final CustomError.Header HEADER = CustomError.Header.PROCESS_ERROR;

    public XlsxConversionException(String message, Throwable cause) {
        super(message);
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
