package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.common.model.CustomPage;
import com.example.xlsxziptotxtzip.common.model.dto.response.CustomPagingResponse;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper for converting a {@link CustomPage} of {@link LogDto}
 * into a {@link CustomPagingResponse} of {@link LogResponse},
 * similar to CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.
 */
@Mapper
public interface CustomPageLogResponseToCustomPagingMapper {

    LogDtoToLogResponseMapper ITEM_MAPPER =
            LogDtoToLogResponseMapper.initialize();

    /**
     * Converts a {@link CustomPage} of {@link LogDto} into a
     * {@link CustomPagingResponse} of {@link LogResponse}.
     * <p>
     * The paging metadata (page number, page size, total element count, total page count)
     * is copied from the {@link CustomPage}, while the content list is converted
     * using {@link #toResponseList(List)}.
     * </p>
     *
     * @param page the page of {@link LogDto} to transform; may be {@code null}
     * @return a {@link CustomPagingResponse} of {@link LogResponse}, or {@code null}
     *         if {@code page} is {@code null}
     */
    default CustomPagingResponse<LogResponse> toPagingResponse(CustomPage<LogDto> page) {
        if (page == null) {
            return null;
        }

        return CustomPagingResponse.<LogResponse>builder()
                .content(toResponseList(page.getContent()))
                .totalElementCount(page.getTotalElementCount())
                .totalPageCount(page.getTotalPageCount())
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .build();
    }

    /**
     * Maps a list of {@link LogDto} objects to a list of {@link LogResponse} objects
     * using {@link LogDtoToLogResponseMapper}.
     *
     * @param list the list of {@link LogDto} instances to convert; may be {@code null}
     * @return an immutable list of {@link LogResponse} objects; never {@code null}
     */
    default List<LogResponse> toResponseList(List<LogDto> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .map(ITEM_MAPPER::map)
                .toList();
    }

    /**
     * Provides a singleton instance of this mapper using MapStruct's {@link Mappers}
     * factory. This is intended for use in places where dependency injection is not
     * leveraged or a simple static access pattern is preferred.
     *
     * @return the {@link CustomPageLogResponseToCustomPagingMapper} instance created by MapStruct
     */
    static CustomPageLogResponseToCustomPagingMapper initialize() {
        return Mappers.getMapper(CustomPageLogResponseToCustomPagingMapper.class);
    }

}
