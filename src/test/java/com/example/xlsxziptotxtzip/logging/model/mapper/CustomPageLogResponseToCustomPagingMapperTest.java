package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.dto.response.CustomPagingResponse;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomPageLogResponseToCustomPagingMapperTest {

    private final CustomPageLogResponseToCustomPagingMapper mapper =
            Mappers.getMapper(CustomPageLogResponseToCustomPagingMapper.class);

    @Test
    void toPagingResponse_whenPageIsNull_returnsNull() {

        // given
        CustomPage<LogDto> page = null;

        // when
        CustomPagingResponse<LogResponse> result = mapper.toPagingResponse(page);

        // then
        assertNull(result);

    }

    @Test
    void toPagingResponse_whenPageIsNotNull_mapsMetadataAndContent() {

        // Given
        LogDto dto1 = LogDto.builder()
                .id("1")
                .message("first")
                .endpoint("/api/test1")
                .method("GET")
                .status(HttpStatus.OK)
                .errorType(null)
                .operation("op1")
                .time(LocalDateTime.now())
                .build();

        LogDto dto2 = LogDto.builder()
                .id("2")
                .message("second")
                .endpoint("/api/test2")
                .method("POST")
                .status(HttpStatus.BAD_REQUEST)
                .errorType("SomeException")
                .operation("op2")
                .time(LocalDateTime.now())
                .build();

        CustomPage<LogDto> page = CustomPage.<LogDto>builder()
                .content(List.of(dto1, dto2))
                .pageNumber(3)
                .pageSize(20)
                .totalElementCount(42L)
                .totalPageCount(5)
                .build();

        // When
        CustomPagingResponse<LogResponse> result = mapper.toPagingResponse(page);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertEquals(42L, result.getTotalElementCount());
        assertEquals(5, result.getTotalPageCount());

        List<LogResponse> content = result.getContent();
        assertNotNull(content);
        assertEquals(2, content.size());

        LogResponse resp1 = content.get(0);
        LogResponse resp2 = content.get(1);

        assertEquals("1", resp1.getId());
        assertEquals("first", resp1.getMessage());
        assertEquals("/api/test1", resp1.getEndpoint());
        assertEquals("GET", resp1.getMethod());
        assertEquals(HttpStatus.OK, resp1.getStatus());

        assertEquals("2", resp2.getId());
        assertEquals("second", resp2.getMessage());
        assertEquals("/api/test2", resp2.getEndpoint());
        assertEquals("POST", resp2.getMethod());
        assertEquals(HttpStatus.BAD_REQUEST, resp2.getStatus());
        assertEquals("SomeException", resp2.getErrorType());

    }

    @Test
    void toResponseList_whenListIsNull_returnsEmptyList() {

        // Given
        List<LogDto> list = null;

        // when
        List<LogResponse> result = mapper.toResponseList(list);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    void toResponseList_whenListIsNotNull_mapsEachItem() {

        // Given
        LogDto dto = LogDto.builder()
                .id("99")
                .message("mapped")
                .endpoint("/api/logs")
                .method("GET")
                .status(HttpStatus.OK)
                .errorType(null)
                .operation("listLogs")
                .time(LocalDateTime.now())
                .build();

        List<LogDto> list = List.of(dto);

        // When
        List<LogResponse> result = mapper.toResponseList(list);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        LogResponse resp = result.getFirst();
        assertEquals("99", resp.getId());
        assertEquals("mapped", resp.getMessage());
        assertEquals("/api/logs", resp.getEndpoint());
        assertEquals("GET", resp.getMethod());
        assertEquals(HttpStatus.OK, resp.getStatus());
        assertEquals("listLogs", resp.getOperation());

    }

}