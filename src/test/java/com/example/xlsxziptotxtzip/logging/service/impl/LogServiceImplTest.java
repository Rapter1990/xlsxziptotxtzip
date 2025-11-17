package com.example.xlsxziptotxtzip.logging.service.impl;

import com.example.xlsxziptotxtzip.base.AbstractBaseServiceTest;
import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.CustomPaging;
import com.example.xlsxziptotxtzip.common.model.CustomSorting;
import com.example.xlsxziptotxtzip.common.model.dto.request.CustomPagingRequest;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;
import com.example.xlsxziptotxtzip.logging.repository.LogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogServiceImplTest extends AbstractBaseServiceTest{

    @InjectMocks
    private LogServiceImpl logService;

    @Mock
    private LogRepository logRepository;

    @Test
    void saveLogToDatabase_setsCurrentTime_andCallsRepositorySave() {

        // Given
        LogEntity entity = LogEntity.builder()
                .id("123")
                .message("test log")
                .build();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        when(logRepository.save(any(LogEntity.class))).thenReturn(entity);

        // Then
        logService.saveLogToDatabase(entity);

        assertThat(entity.getTime()).isNotNull();
        assertThat(entity.getTime()).isAfter(before);
        assertThat(entity.getTime()).isBeforeOrEqualTo(LocalDateTime.now());

        // Verify
        verify(logRepository, times(1)).save(entity);

    }

    @Test
    void listLogs_whenPagingRequestIsProvided_usesRequestPageable_andMapsToCustomPage() {

        // Given
        CustomPagingRequest pagingRequest = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder()
                        .pageNumber(1)
                        .pageSize(5)
                        .build())
                .sorting(CustomSorting.builder()
                        .sortBy("time")
                        .sortDirection("DESC")
                        .build())
                .build();

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "time"));

        LogEntity e1 = LogEntity.builder()
                .id("1")
                .message("log-1")
                .time(LocalDateTime.now())
                .build();

        LogEntity e2 = LogEntity.builder()
                .id("2")
                .message("log-2")
                .time(LocalDateTime.now())
                .build();

        Page<LogEntity> page = new PageImpl<>(List.of(e1, e2), pageable, 10);

        // When
        when(logRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Then
        CustomPage<LogDto> result = logService.listLogs(pagingRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElementCount()).isEqualTo(10);
        assertThat(result.getTotalPageCount()).isEqualTo(page.getTotalPages());

        // then
        verify(logRepository, times(1)).findAll(any(Pageable.class));

    }

    @Test
    void listLogs_whenPagingRequestIsNull_usesDefaultPageRequest_andMapsToCustomPage() {

        // Given
        Pageable defaultPageable = PageRequest.of(0, 20);

        LogEntity e1 = LogEntity.builder()
                .id("1")
                .message("log-default")
                .time(LocalDateTime.now())
                .build();

        Page<LogEntity> page = new PageImpl<>(List.of(e1), defaultPageable, 1);

        // when
        when(logRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Then
        CustomPage<LogDto> result = logService.listLogs(null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getTotalElementCount()).isEqualTo(1);
        assertThat(result.getTotalPageCount()).isEqualTo(1);

        // Verify
        verify(logRepository, times(1)).findAll(any(Pageable.class));

    }

}