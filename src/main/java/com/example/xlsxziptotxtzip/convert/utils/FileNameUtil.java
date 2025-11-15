package com.example.xlsxziptotxtzip.convert.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for building file names used in responses.
 */
@UtilityClass
public class FileNameUtil {

    private static final DateTimeFormatter ZIP_TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String BASE_CONVERTED_ZIP_NAME = "converted-txt-files.zip";

    /**
     * Builds a file name like:
     *   yyyyMMddHHmmss_converted-txt-files.zip
     *
     * @return timestamp-prefixed file name
     */
    public String buildConvertedTxtZipFileName() {
        String timestamp = LocalDateTime.now().format(ZIP_TS_FORMATTER);
        return timestamp + "_" + BASE_CONVERTED_ZIP_NAME;
    }

}
