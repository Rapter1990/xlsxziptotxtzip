package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogDtoToLogResponseMapperTest {

    private final LogDtoToLogResponseMapper mapper =
            Mappers.getMapper(LogDtoToLogResponseMapper.class);

    @Test
    void map_whenSourceIsNull_returnsNull() {

        // Given
        LogDto source = null;

        // When
        LogResponse result = mapper.map(source);

        // Then
        assertNull(result);

    }

    @Test
    void mapCollection_whenSourcesIsNull_returnsNull() {

        // Given
        List<LogDto> sources = null;

        // When
        List<LogResponse> result = mapper.map(sources);

        // Then
        assertNull(result);

    }

    @Test
    void map_whenSourceIsNotNull_mapsAllFields() {

        // Given
        LocalDateTime now = LocalDateTime.now();

        LogDto dto = LogDto.builder()
                .id("42")
                .message("Test message")
                .endpoint("/api/logs")
                .method("GET")
                .status(HttpStatus.OK)
                .errorType("SomeException")
                .operation("listLogs")
                .time(now)
                .build();

        // When
        LogResponse result = mapper.map(dto);

        // Then
        assertNotNull(result);
        assertEquals("42", result.getId());
        assertEquals("Test message", result.getMessage());
        assertEquals("/api/logs", result.getEndpoint());
        assertEquals("GET", result.getMethod());
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals("SomeException", result.getErrorType());
        assertEquals("listLogs", result.getOperation());
        assertEquals(now, result.getTime());

    }

    @Test
    void mapCollection_whenListHasItems_mapsEachItem() {

        // Given
        LocalDateTime now = LocalDateTime.now();

        LogDto dto1 = LogDto.builder()
                .id("1")
                .message("First")
                .endpoint("/api/first")
                .method("POST")
                .status(HttpStatus.CREATED)
                .errorType(null)
                .operation("create")
                .time(now)
                .build();

        LogDto dto2 = LogDto.builder()
                .id("2")
                .message("Second")
                .endpoint("/api/second")
                .method("DELETE")
                .status(HttpStatus.NO_CONTENT)
                .errorType("SomeError")
                .operation("delete")
                .time(now.plusSeconds(1))
                .build();

        List<LogDto> sources = List.of(dto1, dto2);

        // When
        List<LogResponse> result = mapper.map(sources);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        LogResponse r1 = result.get(0);
        LogResponse r2 = result.get(1);

        assertEquals("1", r1.getId());
        assertEquals("First", r1.getMessage());
        assertEquals("/api/first", r1.getEndpoint());
        assertEquals("POST", r1.getMethod());
        assertEquals(HttpStatus.CREATED, r1.getStatus());
        assertEquals("create", r1.getOperation());
        assertEquals(now, r1.getTime());

        assertEquals("2", r2.getId());
        assertEquals("Second", r2.getMessage());
        assertEquals("/api/second", r2.getEndpoint());
        assertEquals("DELETE", r2.getMethod());
        assertEquals(HttpStatus.NO_CONTENT, r2.getStatus());
        assertEquals("SomeError", r2.getErrorType());
        assertEquals("delete", r2.getOperation());
        assertEquals(now.plusSeconds(1), r2.getTime());

    }

}