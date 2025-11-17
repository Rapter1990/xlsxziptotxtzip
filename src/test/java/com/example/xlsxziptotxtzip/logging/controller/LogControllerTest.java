package com.example.xlsxziptotxtzip.logging.controller;

import com.example.xlsxziptotxtzip.base.AbstractRestControllerTest;
import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.CustomPaging;
import com.example.xlsxziptotxtzip.common.model.CustomSorting;
import com.example.xlsxziptotxtzip.common.model.dto.request.CustomPagingRequest;
import com.example.xlsxziptotxtzip.common.model.dto.response.CustomPagingResponse;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import com.example.xlsxziptotxtzip.logging.model.mapper.CustomPageLogResponseToCustomPagingMapper;
import com.example.xlsxziptotxtzip.logging.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LogControllerTest extends AbstractRestControllerTest {

    private static final String URL = "/api/logs/list";

    @MockitoBean
    private LogService logService;

    @MockitoBean
    private CustomPageLogResponseToCustomPagingMapper logPageMapper;

    @Autowired
    private LogController logController;

    @BeforeEach
    void setUp() {
        // Controller creates mapper with initialize(), so we replace it with our @MockitoBean mock
        ReflectionTestUtils.setField(logController, "logPageMapper", logPageMapper);
    }

    @Test
    void listLogs_whenValidRequest_returnsOk_andUsesServiceAndMapper() throws Exception {

        // given
        CustomPagingRequest pagingRequest = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder()
                        .pageNumber(1)
                        .pageSize(10)
                        .build())
                .sorting(CustomSorting.builder()
                        .sortBy("time")
                        .sortDirection("DESC")
                        .build())
                .build();

        String body = objectMapper.writeValueAsString(pagingRequest);

        @SuppressWarnings("unchecked")
        CustomPage<LogDto> customPage = mock(CustomPage.class);

        @SuppressWarnings("unchecked")
        CustomPagingResponse<LogResponse> pagingResponse = mock(CustomPagingResponse.class);

        when(logService.listLogs(any(CustomPagingRequest.class))).thenReturn(customPage);
        when(logPageMapper.toPagingResponse(customPage)).thenReturn(pagingResponse);

        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(logService, times(1)).listLogs(any(CustomPagingRequest.class));
        verify(logPageMapper, times(1)).toPagingResponse(customPage);
    }


    @Test
    void listLogs_whenPageNumberLessThanOne_returnsBadRequest_andDoesNotCallServiceOrMapper() throws Exception {
        // given: invalid pageNumber (0) → violates @Min(1)
        CustomPaging invalidPagination = CustomPaging.builder()
                .pageNumber(0)
                .pageSize(10)
                .build();

        CustomPagingRequest invalidRequest = CustomPagingRequest.builder()
                .pagination(invalidPagination)
                .build();

        String body = objectMapper.writeValueAsString(invalidRequest);

        // when / then
        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.header").value("VALIDATION ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        // Verify
        verify(logService, never()).listLogs(any());
        verify(logPageMapper, never()).toPagingResponse(any());

    }

}
