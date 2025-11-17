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
     * Get singleton instance of the mapper.
     */
    static LogDtoToLogResponseMapper initialize() {
        return Mappers.getMapper(LogDtoToLogResponseMapper.class);
    }

}
