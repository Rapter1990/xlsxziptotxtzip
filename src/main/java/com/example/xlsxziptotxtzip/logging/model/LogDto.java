package com.example.xlsxziptotxtzip.logging.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Lightweight DTO view of {@link com.example.xlsxziptotxtzip.logging.model.entity.LogEntity}.
 */
@Getter
@Builder
public class LogDto {

    private String id;

    private String message;

    private String endpoint;

    private String method;

    private HttpStatus status;

    private String errorType;

    private String operation;

    private LocalDateTime time;

}
