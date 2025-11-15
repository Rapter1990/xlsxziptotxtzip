package com.example.xlsxziptotxtzip.convert.utils;

import com.example.xlsxziptotxtzip.convert.exception.XlsxConversionException;
import com.example.xlsxziptotxtzip.convert.model.dto.SheetContent;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting XLSX content to aligned TXT.
 */
@UtilityClass
public class XlsxToTextUtil {

    private static final int MAX_COLUMN_WIDTH = 80;
    private static final int PADDING = 2;

    /**
     * Converts an XLSX document (provided as an {@link InputStream}) into
     * an aligned plain-text table.
     * <p>
     * The conversion process:
     * <ol>
     *     <li>Reads all rows and cells from the first sheet.</li>
     *     <li>Computes the maximum width for each column.</li>
     *     <li>Detects columns that should be right-aligned (e.g. {@code postal_code}).</li>
     *     <li>Renders a header row, header separator, and data rows into text.</li>
     * </ol>
     * On any error, wraps the failure in {@link XlsxConversionException}.
     * </p>
     *
     * @param inputStream the XLSX file content
     * @return a plain-text representation of the first sheet
     */
    public String convertXlsxToAlignedText(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // first sheet only
            DataFormatter formatter = new DataFormatter(); // uses default locale

            // 1) Read all rows and detect max column count
            SheetContent sheetContent = extractSheetContent(sheet, formatter);
            List<List<String>> rows = sheetContent.rows();
            int maxColumns = sheetContent.maxColumns();

            // 2) Compute column widths
            int[] columnWidths = computeColumnWidths(rows, maxColumns);

            // 3) Detect right-aligned columns (postal_code)
            boolean[] forceRightAlignColumn = detectRightAlignedColumns(rows, maxColumns);

            // 4) Render final text
            return renderAlignedText(rows, maxColumns, columnWidths, forceRightAlignColumn);
        } catch (Exception e) {
            throw new XlsxConversionException("Failed to convert XLSX to text", e);
        }
    }

    /**
     * Reads all rows and cells from the given {@link Sheet} and returns both
     * the list of row values and the maximum number of columns found.
     * <p>
     * Each row is converted into a {@link List} of {@link String} values using
     * the provided {@link DataFormatter}.
     * </p>
     *
     * @param sheet     the POI {@link Sheet} to read from
     * @param formatter the {@link DataFormatter} used to format cell values
     * @return a {@link SheetContent} record containing rows and max column count
     */
    private static SheetContent extractSheetContent(Sheet sheet, DataFormatter formatter) {
        List<List<String>> rows = new ArrayList<>();
        int maxColumns = 0;

        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            short lastCellNum = row.getLastCellNum(); // -1 if empty row
            int effectiveLastCellNum = Math.max(lastCellNum, (short) 0);

            maxColumns = Math.max(maxColumns, effectiveLastCellNum);

            for (int cn = 0; cn < effectiveLastCellNum; cn++) {
                Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = (cell == null) ? "" : formatter.formatCellValue(cell);
                rowData.add(value != null ? value : "");
            }

            rows.add(rowData);
        }

        return new SheetContent(rows, maxColumns);
    }

    /**
     * Computes the maximum width per column across all rows, then clamps each
     * width to {@link #MAX_COLUMN_WIDTH}.
     *
     * @param rows       the list of rows with cell values
     * @param maxColumns the maximum number of columns found in the sheet
     * @return an array of column widths (length = {@code maxColumns})
     */
    private static int[] computeColumnWidths(List<List<String>> rows, int maxColumns) {
        int[] columnWidths = new int[maxColumns];

        for (List<String> row : rows) {
            for (int i = 0; i < maxColumns; i++) {
                String value = (i < row.size()) ? row.get(i) : "";
                int len = (value != null) ? value.length() : 0;
                columnWidths[i] = Math.max(columnWidths[i], len);
            }
        }

        // Clamp each column’s width to MAX_COLUMN_WIDTH
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = Math.min(columnWidths[i], MAX_COLUMN_WIDTH);
        }

        return columnWidths;
    }

    /**
     * Detects which columns should always be right-aligned, based on header names.
     * <p>
     * Currently, columns whose header is {@code "postal_code"} or {@code "postal code"}
     * (case-insensitive) are marked as right-aligned.
     * </p>
     *
     * @param rows       all sheet rows, with row {@code 0} assumed to be the header
     * @param maxColumns the maximum number of columns
     * @return a boolean array where each index represents whether the column is forced to be right-aligned
     */
    private static boolean[] detectRightAlignedColumns(List<List<String>> rows, int maxColumns) {
        boolean[] forceRightAlignColumn = new boolean[maxColumns];

        if (!rows.isEmpty()) {
            List<String> headerRow = rows.get(0);
            for (int i = 0; i < maxColumns; i++) {
                String h = (i < headerRow.size() && headerRow.get(i) != null)
                        ? headerRow.get(i).trim().toLowerCase()
                        : "";
                if ("postal_code".equals(h) || "postal code".equals(h)) {
                    forceRightAlignColumn[i] = true;
                }
            }
        }

        return forceRightAlignColumn;
    }

    /**
     * Renders the entire sheet content as aligned text.
     * <p>
     * Renders the header row (first row), a separator line, then all data rows
     * using {@link #renderRow(List, int, int[], boolean[], boolean)}.
     * </p>
     *
     * @param rows                 all rows from the sheet
     * @param maxColumns           max column count across rows
     * @param columnWidths         calculated width of each column
     * @param forceRightAlignColumn per-column flags indicating forced right alignment
     * @return the full text table as a single string
     */
    private static String renderAlignedText(List<List<String>> rows,
                                            int maxColumns,
                                            int[] columnWidths,
                                            boolean[] forceRightAlignColumn) {

        StringBuilder sb = new StringBuilder();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            boolean headerRow = (rowIndex == 0);

            // 1) Render the row itself
            sb.append(renderRow(row, maxColumns, columnWidths, forceRightAlignColumn, headerRow))
                    .append(System.lineSeparator());

            // 2) After header row, render separator line once
            if (headerRow && !rows.isEmpty()) {
                sb.append(renderHeaderSeparator(maxColumns, columnWidths))
                        .append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    /**
     * Renders a single row (header or data) into a string, without trailing newline.
     *
     * @param row                  list of cell values for the row
     * @param maxColumns           max column count across all rows
     * @param columnWidths         per-column width constraints
     * @param forceRightAlignColumn flags for each column indicating forced right alignment
     * @param headerRow            whether this row is the header row
     * @return formatted line for the row
     */
    private static String renderRow(List<String> row,
                                    int maxColumns,
                                    int[] columnWidths,
                                    boolean[] forceRightAlignColumn,
                                    boolean headerRow) {

        StringBuilder line = new StringBuilder();

        for (int i = 0; i < maxColumns; i++) {
            String raw = (i < row.size()) ? row.get(i) : "";
            String safe = raw != null ? raw : "";

            // Truncate to column width
            String truncated = truncate(safe, columnWidths[i]);

            // Decide alignment & format
            String formatted = formatCell(truncated, columnWidths[i], headerRow, forceRightAlignColumn[i]);

            line.append(formatted);

            // Add fixed gap between columns (but not after the last one)
            if (i < maxColumns - 1) {
                line.append(" ".repeat(PADDING));
            }
        }

        return line.toString();
    }

    /**
     * Formats a single cell value, including truncation and alignment.
     * <p>
     * Header cells are always left-aligned. Data cells are right-aligned if
     * {@code forceRightAlign} is {@code true} or if {@link #isNumeric(String)} returns true.
     * Otherwise, they are left-aligned.
     * </p>
     *
     * @param value            the cell value (already truncated)
     * @param columnWidth      the target width of the column
     * @param headerRow        whether the value belongs to the header row
     * @param forceRightAlign  whether this column should always be right-aligned
     * @return the padded cell string
     */
    private static String formatCell(String value,
                                     int columnWidth,
                                     boolean headerRow,
                                     boolean forceRightAlign) {

        if (headerRow) {
            // Header always left-aligned
            return padRight(value, columnWidth);
        }

        boolean alignRight = forceRightAlign || isNumeric(value);

        return alignRight
                ? padLeft(value, columnWidth)
                : padRight(value, columnWidth);
    }

    /**
     * Renders the separator line that appears after the header row.
     * <p>
     * The separator consists of dashes for each column, with {@link #PADDING}
     * spaces between columns.
     * </p>
     *
     * @param maxColumns   max column count
     * @param columnWidths per-column width constraints
     * @return the formatted separator line as a string (no newline)
     */
    private static String renderHeaderSeparator(int maxColumns, int[] columnWidths) {
        StringBuilder sep = new StringBuilder();

        for (int i = 0; i < maxColumns; i++) {
            sep.append("-".repeat(columnWidths[i]));
            if (i < maxColumns - 1) {
                sep.append(" ".repeat(PADDING));
            }
        }

        return sep.toString();
    }

    /**
     * Pads the given string on the right with spaces until it reaches the given width.
     *
     * @param s     input string (may be {@code null})
     * @param width target width in characters
     * @return right-padded string
     */
    private static String padRight(String s, int width) {
        if (s == null) {
            s = "";
        }
        int pad = width - s.length();
        if (pad <= 0) return s;
        StringBuilder sb = new StringBuilder(width);
        sb.append(s);
        for (int i = 0; i < pad; i++) sb.append(' ');
        return sb.toString();
    }

    /**
     * Pads the given string on the left with spaces until it reaches the given width.
     *
     * @param s     input string (may be {@code null})
     * @param width target width in characters
     * @return left-padded string
     */
    private static String padLeft(String s, int width) {
        if (s == null) {
            s = "";
        }
        int pad = width - s.length();
        if (pad <= 0) return s;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    /**
     * Truncates a string to a maximum length and appends an ellipsis character
     * ({@code …}) if truncation occurs.
     *
     * @param s   the input string (may be {@code null})
     * @param max maximum allowed length
     * @return the original string if shorter than or equal to {@code max},
     * or a truncated version with ellipsis appended
     */
    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (max <= 1) {
            return s.isEmpty() ? "" : s.substring(0, 1);
        }
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }

    /**
     * Detects numeric-looking values, supporting:
     * - plain integers              (e.g., "123", "007")
     * - decimals with dot/comma     (e.g., "3.14", "3,14")
     * - code-like values with digits, spaces, or hyphens only
     *   (e.g., "14870-000", "4960-010", "91901")
     */
    private static boolean isNumeric(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        // Normalize comma to dot for decimal check
        String normalized = trimmed.replace(',', '.');

        // 1) Standard integer / decimal
        if (normalized.matches("[-+]?\\d+(\\.\\d+)?")) {
            return true;
        }

        // 2) Code-like: starts with digit, then digits / spaces / hyphens only
        return trimmed.matches("\\d[\\d\\s-]*");

    }

}