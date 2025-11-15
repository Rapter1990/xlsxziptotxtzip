package com.example.xlsxziptotxtzip.convert.model.dto;

import java.util.List;

/**
 * Simple container for preprocessed sheet data used during XLSX → TXT conversion.
 */
public record SheetContent(List<List<String>> rows, int maxColumns) {}
