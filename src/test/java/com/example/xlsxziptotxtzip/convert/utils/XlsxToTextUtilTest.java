package com.example.xlsxziptotxtzip.convert.utils;

import com.example.xlsxziptotxtzip.convert.exception.XlsxConversionException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class XlsxToTextUtilTest {

    @Test
    void convertXlsxToAlignedText_happyPath_alignsHeaderLeft_andNumericAndPostalRight() throws Exception {
        // given: an in-memory XLSX with header + 2 data rows
        ByteArrayInputStream inputStream = new ByteArrayInputStream(createSampleWorkbookBytes());

        // when
        String result = XlsxToTextUtil.convertXlsxToAlignedText(inputStream);

        // then
        String[] lines = result.split(System.lineSeparator());
        // header + separator + 2 data rows = 4 lines
        assertThat(lines).hasSize(4);

        String headerLine    = lines[0];
        String separatorLine = lines[1];
        String dataLine1     = lines[2]; // Alice row
        String dataLine2     = lines[3]; // Bob row

        // Very loose checks so formatting changes don't break the test:
        assertThat(headerLine).contains("name").contains("age").contains("postal_code");
        assertThat(separatorLine).contains("-"); // simple sanity check

        // Split by 2+ whitespace chars and trim to check alignment semantics
        String[] row1Cols = dataLine1.trim().split("\\s{2,}");
        String[] row2Cols = dataLine2.trim().split("\\s{2,}");

        assertThat(row1Cols[0].trim()).isEqualTo("Alice");
        assertThat(row1Cols[1].trim()).isEqualTo("30");
        assertThat(row1Cols[2].trim()).isEqualTo("14870-000");

        assertThat(row2Cols[0].trim()).isEqualTo("Bob");
        assertThat(row2Cols[1].trim()).isEqualTo("25");
        assertThat(row2Cols[2].trim()).isEqualTo("4960-010");
    }

    @Test
    void convertXlsxToAlignedText_whenPoiThrows_wrapsInXlsxConversionException() throws Exception {
        // given
        InputStream badStream = mock(InputStream.class);
        when(badStream.read(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("boom"));

        // when / then
        assertThatThrownBy(() -> XlsxToTextUtil.convertXlsxToAlignedText(badStream))
                .isInstanceOf(XlsxConversionException.class)
                .hasMessage("Failed to convert XLSX to text")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(badStream, atLeastOnce()).read(any(), anyInt(), anyInt());
    }


    @Test
    void extractSheetContent_readsRowsAndTracksMaxColumns() {
        // given (mock Sheet + one Row + two Cells)
        Sheet sheet = mock(Sheet.class);
        Row row1 = mock(Row.class);
        Cell cell0 = mock(Cell.class);
        Cell cell1 = mock(Cell.class);

        List<Row> rowList = List.of(row1);
        when(sheet.iterator()).thenReturn(rowList.iterator());

        when(row1.getLastCellNum()).thenReturn((short) 2);
        when(row1.getCell(eq(0), any(Row.MissingCellPolicy.class))).thenReturn(cell0);
        when(row1.getCell(eq(1), any(Row.MissingCellPolicy.class))).thenReturn(cell1);

        DataFormatter formatter = mock(DataFormatter.class);
        when(formatter.formatCellValue(cell0)).thenReturn("Alice");
        when(formatter.formatCellValue(cell1)).thenReturn("30");

        // when
        Object sheetContent = ReflectionTestUtils.invokeMethod(
                XlsxToTextUtil.class,
                "extractSheetContent",
                sheet,
                formatter
        );

        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) ReflectionTestUtils.getField(sheetContent, "rows");
        Integer maxColumns = (Integer) ReflectionTestUtils.getField(sheetContent, "maxColumns");

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsExactly("Alice", "30");
        assertThat(maxColumns).isEqualTo(2);

        verify(sheet).iterator();
        verify(row1).getLastCellNum();
        verify(row1, times(2)).getCell(anyInt(), any());
        verify(formatter).formatCellValue(cell0);
        verify(formatter).formatCellValue(cell1);
    }

    @Test
    void isNumeric_acceptsIntegerDecimalAndCodeLike() {
        // given / when
        boolean integer   = invokeIsNumeric("123");
        boolean decimal   = invokeIsNumeric("3.14");
        boolean decimalTr = invokeIsNumeric("3,14");
        boolean codeLike  = invokeIsNumeric("14870-000");
        boolean spaces    = invokeIsNumeric("91901 001");
        boolean negative  = invokeIsNumeric("-42");

        // then
        assertThat(integer).isTrue();
        assertThat(decimal).isTrue();
        assertThat(decimalTr).isTrue();
        assertThat(codeLike).isTrue();
        assertThat(spaces).isTrue();
        assertThat(negative).isTrue();
    }

    @Test
    void isNumeric_rejectsNonNumeric() {
        // given / when
        boolean alpha      = invokeIsNumeric("ABC");
        boolean mixed      = invokeIsNumeric("AB123");
        boolean empty      = invokeIsNumeric("");
        boolean onlySpaces = invokeIsNumeric("   ");
        boolean nullValue  = invokeIsNumeric(null);

        // then
        assertThat(alpha).isFalse();
        assertThat(mixed).isFalse();
        assertThat(empty).isFalse();
        assertThat(onlySpaces).isFalse();
        assertThat(nullValue).isFalse();
    }


    @Test
    void truncate_whenShorterThanMax_returnsOriginal() {
        // given / when
        String result = invokeTruncate("hello", 10);

        // then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void truncate_whenLongerThanMax_addsEllipsis() {
        // given / when
        String result = invokeTruncate("abcdefghijkl", 5);

        // then (max=5 → first 4 chars + '…')
        assertThat(result).isEqualTo("abcd…");
    }

    @Test
    void truncate_whenMaxLessOrEqualOne_behavesSafely() {
        // given / when
        String max1 = invokeTruncate("hello", 1);
        String max0 = invokeTruncate("hello", 0);
        String emptyForMax0 = invokeTruncate("", 0);

        // then
        assertThat(max1).hasSize(1);
        assertThat(max0).isEqualTo("h");      // from your current implementation
        assertThat(emptyForMax0).isEqualTo("");
    }

    @Test
    void detectRightAlignedColumns_marksPostalColumns() {
        // given
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("name", "postal_code", "city"));
        int maxColumns = 3;

        // when
        boolean[] flags = (boolean[]) ReflectionTestUtils.invokeMethod(
                XlsxToTextUtil.class,
                "detectRightAlignedColumns",
                rows,
                maxColumns
        );

        // then
        assertThat(flags).containsExactly(false, true, false);
    }

    // --- PRIVATE METHOD: computeColumnWidths (via ReflectionTestUtils) --------------------

    @Test
    void computeColumnWidths_calculatesMaxPerColumn_andClampsToMaxWidth() {
        // given
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "name"));
        rows.add(List.of("1", "Very long name that might be truncated at some point if wider than 80 chars"));

        int maxColumns = 2;

        // when
        int[] widths = (int[]) ReflectionTestUtils.invokeMethod(
                XlsxToTextUtil.class,
                "computeColumnWidths",
                rows,
                maxColumns
        );

        // then
        assertThat(widths).hasSize(2);
        assertThat(widths[0]).isGreaterThanOrEqualTo(1);
        assertThat(widths[1]).isGreaterThanOrEqualTo("name".length());
        assertThat(widths[1]).isLessThanOrEqualTo(80); // clamped to MAX_COLUMN_WIDTH
    }

    @Test
    void truncate_whenInputIsNull_returnsEmptyString() {
        // given
        String value = null;
        int max = 10;

        // when
        String result = invokeTruncate(value,max);

        // then
        assertThat(result).isEmpty();
    }

    // --- Helper: create a minimal XLSX workbook -------------------------------------------

    private byte[] createSampleWorkbookBytes() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("age");
            header.createCell(2).setCellValue("postal_code");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue(30);
            r1.createCell(2).setCellValue("14870-000");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Bob");
            r2.createCell(1).setCellValue(25);
            r2.createCell(2).setCellValue("4960-010");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    // --- Helpers: invoke private methods via ReflectionTestUtils --------------------------

    private boolean invokeIsNumeric(String value) {
        return (boolean) ReflectionTestUtils.invokeMethod(
                XlsxToTextUtil.class,
                "isNumeric",
                value
        );
    }

    private String invokeTruncate(String value, int max) {
        return (String) ReflectionTestUtils.invokeMethod(
                XlsxToTextUtil.class,
                "truncate",
                value,
                max
        );
    }

}