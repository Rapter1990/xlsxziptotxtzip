package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogEntityToLogDtoMapperTest {

    private final LogEntityToLogDtoMapper mapper =
            Mappers.getMapper(LogEntityToLogDtoMapper.class);

    @Test
    void map_whenSourceIsNull_returnsNull() {

        // Given
        LogEntity source = null;

        // When
        LogDto result = mapper.map(source);

        // Then
        assertNull(result);

    }

    @Test
    void mapCollection_whenSourcesIsNull_returnsNull() {

        // Given
        List<LogEntity> sources = null;

        // When
        List<LogDto> result = mapper.map(sources);

        // Then
        assertNull(result);

    }

    @Test
    void map_whenSourceIsNotNull_mapsAllFields() {

        // Given
        LocalDateTime now = LocalDateTime.now();

        LogEntity entity = LogEntity.builder()
                .id("42")
                .message("Test log")
                .endpoint("/api/logs")
                .method("GET")
                .status(HttpStatus.OK)
                .errorType("SomeException")
                .operation("listLogs")
                .time(now)
                .build();

        // When
        LogDto result = mapper.map(entity);

        // Then
        assertNotNull(result);
        assertEquals("42", result.getId());
        assertEquals("Test log", result.getMessage());
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

        LogEntity e1 = LogEntity.builder()
                .id("1")
                .message("First log")
                .endpoint("/api/first")
                .method("POST")
                .status(HttpStatus.CREATED)
                .errorType(null)
                .operation("create")
                .time(now)
                .build();

        LogEntity e2 = LogEntity.builder()
                .id("2")
                .message("Second log")
                .endpoint("/api/second")
                .method("DELETE")
                .status(HttpStatus.NO_CONTENT)
                .errorType("SomeError")
                .operation("delete")
                .time(now.plusSeconds(1))
                .build();

        List<LogEntity> sources = List.of(e1, e2);

        // When
        List<LogDto> result = mapper.map(sources);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        LogDto d1 = result.get(0);
        LogDto d2 = result.get(1);

        assertEquals("1", d1.getId());
        assertEquals("First log", d1.getMessage());
        assertEquals("/api/first", d1.getEndpoint());
        assertEquals("POST", d1.getMethod());
        assertEquals(HttpStatus.CREATED, d1.getStatus());
        assertEquals("create", d1.getOperation());
        assertEquals(now, d1.getTime());

        assertEquals("2", d2.getId());
        assertEquals("Second log", d2.getMessage());
        assertEquals("/api/second", d2.getEndpoint());
        assertEquals("DELETE", d2.getMethod());
        assertEquals(HttpStatus.NO_CONTENT, d2.getStatus());
        assertEquals("SomeError", d2.getErrorType());
        assertEquals("delete", d2.getOperation());
        assertEquals(now.plusSeconds(1), d2.getTime());

    }

}