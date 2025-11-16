package com.example.xlsxziptotxtzip.logging.controller;

import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.dto.request.CustomPagingRequest;
import com.example.xlsxziptotxtzip.common.model.dto.response.CustomPagingResponse;
import com.example.xlsxziptotxtzip.common.model.dto.response.CustomResponse;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import com.example.xlsxziptotxtzip.logging.model.mapper.CustomPageLogResponseToCustomPagingMapper;
import com.example.xlsxziptotxtzip.logging.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for querying application logs.
 * <p>
 * The main endpoint {@link #listLogs(CustomPagingRequest)} allows clients to
 * retrieve logs in a paged form using a {@link CustomPagingRequest} that
 * encapsulates pagination and sorting information.
 * </p>
 * <p>
 * <ol>
 *     <li>Delegates to {@link LogService#listLogs(CustomPagingRequest)} to load a {@link CustomPage}
 *         of {@link LogDto}.</li>
 *     <li>Uses {@link CustomPageLogResponseToCustomPagingMapper} to convert that page into a
 *         {@link CustomPagingResponse} of {@link LogResponse}.</li>
 *     <li>Wraps the result in a {@link CustomResponse} and returns it as a {@link ResponseEntity}.</li>
 * </ol>
 * </p>
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    private final CustomPageLogResponseToCustomPagingMapper logPageMapper =
            CustomPageLogResponseToCustomPagingMapper.initialize();

    /**
     * Lists logs using pagination (and optional sorting) defined by the provided
     * {@link CustomPagingRequest}.
     * <p>
     * The request body is validated with {@link jakarta.validation.Valid}, so
     * invalid pagination parameters (e.g. pageNumber &lt; 1) will be handled by
     * the global {@link com.example.xlsxziptotxtzip.common.exception.GlobalExceptionHandler}.
     * </p>
     *
     * @param pagingRequest the paging and sorting parameters (page number, size, sort field, direction)
     * @return a {@link ResponseEntity} wrapping a {@link CustomResponse} that contains a
     * {@link CustomPagingResponse} of {@link LogResponse} items
     */
    @PostMapping("/list")
    @Operation(summary = "List logs with pagination")
    public ResponseEntity<CustomResponse<CustomPagingResponse<LogResponse>>> listLogs(
            @Valid @RequestBody CustomPagingRequest pagingRequest
    ) {

        CustomPage<LogDto> customPage = logService.listLogs(pagingRequest);

        CustomPagingResponse<LogResponse> pagingResponse = logPageMapper.toPagingResponse(customPage);

        return ResponseEntity.ok(CustomResponse.successOf(pagingResponse));

    }

}
