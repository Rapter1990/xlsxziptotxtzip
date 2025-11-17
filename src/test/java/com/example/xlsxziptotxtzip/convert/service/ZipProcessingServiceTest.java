package com.example.xlsxziptotxtzip.convert.service;

import com.example.xlsxziptotxtzip.convert.exception.ZipProcessingException;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFileSource;
import com.example.xlsxziptotxtzip.convert.model.mapper.ConvertedFileSourceToConvertedFileMapper;
import com.example.xlsxziptotxtzip.convert.service.ZipProcessingService;
import com.example.xlsxziptotxtzip.base.AbstractBaseServiceTest;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class ZipProcessingServiceTest extends AbstractBaseServiceTest {

    @InjectMocks
    private ZipProcessingService zipProcessingService;

    @Mock
    private ConvertedFileSourceToConvertedFileMapper mapper;

    @BeforeEach
    void setUp() {
        // ZipProcessingService has its own initialize(); we override it with our @Mock
        ReflectionTestUtils.setField(zipProcessingService, "mapper", mapper);
    }

    @Test
    void processZip_whenZipContainsXlsxAndNonXlsx_skipsNonXlsx_andMapsXlsxEntries() throws Exception {
        // given: a ZIP with one .txt (skipped) and one .xlsx (processed)
        byte[] zipBytes = createZipWithTxtAndXlsx();

        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));

        ConvertedFile converted = mock(ConvertedFile.class);
        when(mapper.mapFromSource(any(ConvertedFileSource.class))).thenReturn(converted);

        // when
        List<ConvertedFile> result = zipProcessingService.processZip(zipFile);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(converted);

        // verify interactions
        verify(zipFile).getInputStream();
        verify(mapper, times(1)).mapFromSource(any(ConvertedFileSource.class));

        // capture source to assert filename & content basics
        ArgumentCaptor<ConvertedFileSource> sourceCaptor = ArgumentCaptor.forClass(ConvertedFileSource.class);
        verify(mapper).mapFromSource(sourceCaptor.capture());

        ConvertedFileSource usedSource = sourceCaptor.getValue();
        assertThat(usedSource.originalFileName()).isEqualTo("data.xlsx");
        assertThat(usedSource.txtContent()).contains("Alice").contains("Bob");
    }

    @Test
    void processZip_whenZipContainsNoXlsxEntries_returnsEmptyList_andDoesNotCallMapper() throws Exception {
        // given: ZIP with only a .txt entry (no .xlsx)
        byte[] zipBytes = createZipWithOnlyTxt();

        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));

        // when
        List<ConvertedFile> result = zipProcessingService.processZip(zipFile);

        // then
        assertThat(result).isEmpty();

        verify(zipFile).getInputStream();
        verify(mapper, never()).mapFromSource(any());
    }

    @Test
    void processZip_whenGetInputStreamThrowsIOException_wrapsInZipProcessingException() throws Exception {
        // given
        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getInputStream()).thenThrow(new IOException("boom"));

        // when / then
        assertThatThrownBy(() -> zipProcessingService.processZip(zipFile))
                .isInstanceOf(ZipProcessingException.class)
                .hasMessage("Failed to process ZIP: I/O error while reading uploaded ZIP")
                .hasCauseInstanceOf(IOException.class);

        verify(zipFile).getInputStream();
        verify(mapper, never()).mapFromSource(any());
    }

    // --- Helpers -------------------------------------------------------------------------

    private byte[] createZipWithTxtAndXlsx() throws Exception {
        byte[] workbookBytes = createSampleWorkbookBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // non-xlsx entry (should be skipped)
            ZipEntry txtEntry = new ZipEntry("notes.txt");
            zos.putNextEntry(txtEntry);
            zos.write("some notes".getBytes());
            zos.closeEntry();

            // xlsx entry (should be processed)
            ZipEntry xlsxEntry = new ZipEntry("data.xlsx");
            zos.putNextEntry(xlsxEntry);
            zos.write(workbookBytes);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private byte[] createZipWithOnlyTxt() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry txtEntry = new ZipEntry("only-notes.txt");
            zos.putNextEntry(txtEntry);
            zos.write("just text".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private byte[] createSampleWorkbookBytes() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("age");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue(30);

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Bob");
            r2.createCell(1).setCellValue(25);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

}