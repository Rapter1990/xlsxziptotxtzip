package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.common.model.mapper.BaseMapper;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.dto.response.LogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting {@link LogDto} to {@link LogResponse}.
 */
@Mapper
public interface LogDtoToLogResponseMapper extends BaseMapper<LogDto, LogResponse> {

    /**
     * Map a LogDto to a LogResponse.
     */
    @Named("mapFromDto")
    default LogResponse mapFromDto(LogDto source) {
        if (source == null) {
            return null;
        }

        return LogResponse.builder()
                .id(source.getId())
                .message(source.getMessage())
                .endpoint(source.getEndpoint())
                .method(source.getMethod())
                .status(source.getStatus())
                .errorType(source.getErrorType())
                .operation(source.getOperation())
                .time(source.getTime())
                .build();
    }

    /**
     * Get singleton instance of the mapper.
     */
    static LogDtoToLogResponseMapper initialize() {
        return Mappers.getMapper(LogDtoToLogResponseMapper.class);
    }

}
