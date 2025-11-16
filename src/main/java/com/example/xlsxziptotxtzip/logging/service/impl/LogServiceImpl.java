package com.example.xlsxziptotxtzip.logging.service.impl;

import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.dto.request.CustomPagingRequest;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;
import com.example.xlsxziptotxtzip.logging.model.mapper.LogEntityToLogDtoMapper;
import com.example.xlsxziptotxtzip.logging.repository.LogRepository;
import com.example.xlsxziptotxtzip.logging.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link LogService} interface.
 * Handles persistence of log entries to the database.
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;

    private final LogEntityToLogDtoMapper logEntityToLogDtoMapper = LogEntityToLogDtoMapper.initialize();

    /**
     * Saves the given {@link LogEntity} to the database with a current timestamp.
     *
     * @param logEntity the log to persist
     */
    @Override
    public void saveLogToDatabase(final LogEntity logEntity) {
        logEntity.setTime(LocalDateTime.now());
        logRepository.save(logEntity);
    }

    /**
     * Returns a paged list of logs based on the provided {@link CustomPagingRequest}.
     * <p>
     * The method:
     * <ol>
     *     <li>Builds a {@link Pageable} from the given {@link CustomPagingRequest}
     *         using {@link CustomPagingRequest#toPageable()}, or falls back to
     *         {@code PageRequest.of(0, 20)} if the request is {@code null}.</li>
     *     <li>Executes {@link LogRepository#findAll(Pageable)} to fetch a {@link Page} of {@link LogEntity}.</li>
     *     <li>Maps each {@link LogEntity} to {@link LogDto} using {@link LogEntityToLogDtoMapper}.</li>
     *     <li>Wraps the results and paging metadata into a {@link CustomPage} of {@link LogDto}.</li>
     * </ol>
     * </p>
     *
     * @param pagingRequest the paging and optional sorting configuration; may be {@code null}
     * @return a {@link CustomPage} of {@link LogDto} representing the requested slice of logs
     */
    @Override
    public CustomPage<LogDto> listLogs(final CustomPagingRequest pagingRequest) {

        final Pageable pageable = Optional.ofNullable(pagingRequest)
                .map(CustomPagingRequest::toPageable)
                .orElse(PageRequest.of(0, 20));

        final Page<LogEntity> page = logRepository.findAll(pageable);

        final List<LogDto> content = page.getContent().stream()
                .map(logEntityToLogDtoMapper::map)
                .toList();

        return CustomPage.<LogDto>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)      // convert to 1-based
                .pageSize(page.getSize())
                .totalElementCount(page.getTotalElements())
                .totalPageCount(page.getTotalPages())
                .build();
    }

}
