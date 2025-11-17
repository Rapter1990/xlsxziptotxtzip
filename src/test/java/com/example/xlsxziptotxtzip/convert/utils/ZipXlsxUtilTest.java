package com.example.xlsxziptotxtzip.convert.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ZipXlsxUtilTest {

    @Test
    void isXlsxFile_whenNullEntry_returnsFalse() {
        // given
        ZipEntry entry = null;

        // when
        boolean result = ZipXlsxUtil.isXlsxFile(entry);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isXlsxFile_whenDirectory_returnsFalse_andDoesNotReadName() {
        // given
        ZipEntry entry = mock(ZipEntry.class);
        when(entry.isDirectory()).thenReturn(true);

        // when
        boolean result = ZipXlsxUtil.isXlsxFile(entry);

        // then
        assertThat(result).isFalse();
        verify(entry).isDirectory();
        verify(entry, never()).getName();
    }

    @Test
    void isXlsxFile_whenNonXlsxFile_returnsFalse() {
        // given
        ZipEntry entry = mock(ZipEntry.class);
        when(entry.isDirectory()).thenReturn(false);
        when(entry.getName()).thenReturn("docs/readme.txt");

        // when
        boolean result = ZipXlsxUtil.isXlsxFile(entry);

        // then
        assertThat(result).isFalse();
        verify(entry).isDirectory();
        verify(entry).getName();
    }

    @Test
    void isXlsxFile_whenXlsxFile_returnsTrue_caseInsensitive() {
        // given
        ZipEntry entry = mock(ZipEntry.class);
        when(entry.isDirectory()).thenReturn(false);
        when(entry.getName()).thenReturn("folder/My-File.XLSX");

        // when
        boolean result = ZipXlsxUtil.isXlsxFile(entry);

        // then
        assertThat(result).isTrue();
        verify(entry).isDirectory();
        verify(entry).getName();
    }

    @Test
    void isXlsxFile_whenNameIsNull_returnsFalse() {
        // given
        ZipEntry entry = mock(ZipEntry.class);
        when(entry.isDirectory()).thenReturn(false);
        when(entry.getName()).thenReturn(null);

        // when
        boolean result = ZipXlsxUtil.isXlsxFile(entry);

        // then
        assertThat(result).isFalse();
        verify(entry).isDirectory();
        verify(entry).getName();
    }

    @Test
    void readEntryBytes_readsWholeEntryContent() throws Exception {
        // given
        String content = "Hello XLSX ZIP!";
        byte[] zipBytes = createZipWithSingleEntry("file.xlsx", content.getBytes(StandardCharsets.UTF_8));

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            // move to first entry
            ZipEntry entry = zis.getNextEntry();
            assertThat(entry).isNotNull();

            // when
            byte[] resultBytes = ZipXlsxUtil.readEntryBytes(zis);

            // then
            assertThat(new String(resultBytes, StandardCharsets.UTF_8)).isEqualTo(content);
        }
    }

    // helper: creates a ZIP with a single entry in-memory
    private byte[] createZipWithSingleEntry(String entryName, byte[] content) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}