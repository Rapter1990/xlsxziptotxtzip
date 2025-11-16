package com.example.xlsxziptotxtzip.logging.service;

import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.dto.request.CustomPagingRequest;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;

/**
 * Service interface for handling log-related operations.
 */
public interface LogService {

    /**
     * Saves the provided {@link LogEntity} to the database.
     *
     * @param logEntity the log entity to persist
     */
    void saveLogToDatabase(final LogEntity logEntity);

    /**
     * Returns a paged list of logs as {@link LogDto} objects according to the
     * provided {@link CustomPagingRequest}.
     * <p>
     * The {@link CustomPagingRequest} typically includes:
     * <ul>
     *     <li>Pagination information (page number, page size)</li>
     *     <li>Optional sorting configuration</li>
     * </ul>
     * The result is wrapped in a {@link CustomPage} that carries both the content
     * and paging metadata (total elements, total pages, etc.).
     * </p>
     *
     * @param pagingRequest the paging and sorting configuration; may be {@code null}
     *                      to use a default page configuration
     * @return a {@link CustomPage} containing {@link LogDto} entries and paging metadata
     */
    CustomPage<LogDto> listLogs(CustomPagingRequest pagingRequest);


}
