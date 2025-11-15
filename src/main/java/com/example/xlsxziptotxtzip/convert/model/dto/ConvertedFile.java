package com.example.xlsxziptotxtzip.convert.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a converted XLSX file.
 */
@Data
@Builder
public class ConvertedFile {

    private String originalFileName;

    private String txtFileName;

    private String content;

}
