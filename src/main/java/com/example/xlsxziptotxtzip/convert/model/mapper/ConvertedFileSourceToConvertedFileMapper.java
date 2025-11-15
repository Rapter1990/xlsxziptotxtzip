package com.example.xlsxziptotxtzip.convert.model.mapper;

import com.example.xlsxziptotxtzip.common.model.mapper.BaseMapper;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFileSource;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting {@link ConvertedFileSource} to {@link ConvertedFile}.
 */
@Mapper
public interface ConvertedFileSourceToConvertedFileMapper extends BaseMapper<ConvertedFileSource, ConvertedFile> {

    /**
     * Map a ConvertedFileSource to a ConvertedFile.
     *
     * @param source the source containing original file name and txt content
     * @return the ConvertedFile domain object
     */
    @Named("mapFromSource")
    default ConvertedFile mapFromSource(ConvertedFileSource source) {
        if (source == null) {
            return null;
        }

        String originalName = source.originalFileName();
        String txtFileName = originalName != null
                ? originalName.replaceAll("(?i)\\.xlsx$", ".txt")
                : "unknown.txt";

        return ConvertedFile.builder()
                .originalFileName(originalName)
                .txtFileName(txtFileName)
                .content(source.txtContent())
                .build();
    }

    /**
     * Get singleton instance of the mapper.
     *
     * @return mapper instance
     */
    static ConvertedFileSourceToConvertedFileMapper initialize() {
        return Mappers.getMapper(ConvertedFileSourceToConvertedFileMapper.class);
    }

}
