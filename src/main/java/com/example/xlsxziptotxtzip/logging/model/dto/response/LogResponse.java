package com.example.xlsxziptotxtzip.logging.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * API response DTO for log entries.
 */
@Getter
@Builder
public class LogResponse {

    private String id;

    private String message;

    private String endpoint;

    private String method;

    private HttpStatus status;

    private String errorType;

    private String operation;

    private LocalDateTime time;

}
