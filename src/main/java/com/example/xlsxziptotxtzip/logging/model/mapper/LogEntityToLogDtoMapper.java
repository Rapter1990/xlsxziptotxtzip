package com.example.xlsxziptotxtzip.logging.model.mapper;

import com.example.xlsxziptotxtzip.common.model.mapper.BaseMapper;
import com.example.xlsxziptotxtzip.logging.model.LogDto;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting {@link LogEntity} to {@link LogDto}.
 */
@Mapper
public interface LogEntityToLogDtoMapper extends BaseMapper<LogEntity, LogDto> {

    /**
     * Map a LogEntity to a LogDto.
     *
     * @param source the log entity
     * @return the DTO representation
     */
    @Named("mapFromEntity")
    default LogDto mapFromEntity(LogEntity source) {
        if (source == null) {
            return null;
        }

        return LogDto.builder()
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
     *
     * @return mapper instance
     */
    static LogEntityToLogDtoMapper initialize() {
        return Mappers.getMapper(LogEntityToLogDtoMapper.class);
    }

}
