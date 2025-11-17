package com.example.xlsxziptotxtzip.convert.controller;

import com.example.xlsxziptotxtzip.base.AbstractRestControllerTest;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.service.ZipProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class XlsxZipToTxtZipControllerTest extends AbstractRestControllerTest {

    private static final String URL = "/api/upload-zip";

    @MockitoBean
    private ZipProcessingService zipProcessingService;

    @Test
    void uploadZip_whenValidZipAndServiceReturnsFiles_returnsZipAndCallsServiceOnce() throws Exception {

        // Given
        MockMultipartFile inputZip = new MockMultipartFile(
                "file",
                "input.zip",
                "application/zip",
                "dummy-zip-content".getBytes(StandardCharsets.UTF_8)
        );

        ConvertedFile file1 = mock(ConvertedFile.class);
        ConvertedFile file2 = mock(ConvertedFile.class);

        when(file1.getTxtFileName()).thenReturn("first.txt");
        when(file1.getContent()).thenReturn("FIRST_CONTENT");
        when(file2.getTxtFileName()).thenReturn("second.txt");
        when(file2.getContent()).thenReturn("SECOND_CONTENT");

        when(zipProcessingService.processZip(any()))
                .thenReturn(List.of(file1, file2));

        // when
        MvcResult result = mockMvc.perform(
                        multipart(URL)
                                .file(inputZip)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".zip")))
                .andReturn();

        // then (Mockito.verify)
        verify(zipProcessingService).processZip(any());

        // Optional: verify returned ZIP content
        byte[] responseBytes = result.getResponse().getContentAsByteArray();
        Map<String, String> zipEntries = extractZipEntries(responseBytes);

        assertThat(zipEntries)
                .containsEntry("first.txt", "FIRST_CONTENT")
                .containsEntry("second.txt", "SECOND_CONTENT");
    }

    @Test
    void uploadZip_whenFileIsEmpty_returnsBadRequest_andDoesNotCallService() throws Exception {

        // given
        MockMultipartFile emptyZip = new MockMultipartFile(
                "file",
                "empty.zip",
                "application/zip",
                new byte[0] // file.isEmpty() == true
        );

        // when
        mockMvc.perform(
                        multipart(URL)
                                .file(emptyZip)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Uploaded file is empty."));

        // Verify
        verify(zipProcessingService, never()).processZip(any());

    }

    @Test
    void uploadZip_whenServiceReturnsEmptyList_returnsBadRequest_andCallsServiceOnce() throws Exception {

        // Given
        MockMultipartFile inputZip = new MockMultipartFile(
                "file",
                "input.zip",
                "application/zip",
                "dummy-zip-content".getBytes(StandardCharsets.UTF_8)
        );

        // When
        when(zipProcessingService.processZip(any()))
                .thenReturn(List.of()); // no XLSX files detected

        // Then
        mockMvc.perform(
                        multipart(URL)
                                .file(inputZip)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No XLSX files were found in the uploaded ZIP."));

        // Verify
        verify(zipProcessingService).processZip(any());

    }

    @Test
    void uploadZip_whenZipCreationFails_throwsZipProcessingException_andReturnsInternalServerError() throws Exception {

        // Given
        MockMultipartFile inputZip = new MockMultipartFile(
                "file",
                "input.zip",
                "application/zip",
                "dummy-zip-content".getBytes(StandardCharsets.UTF_8)
        );

        ConvertedFile bad = mock(ConvertedFile.class);

        // When
        when(bad.getTxtFileName()).thenReturn("bad.txt");
        when(bad.getContent()).thenThrow(new RuntimeException("boom"));
        when(zipProcessingService.processZip(any()))
                .thenReturn(List.of(bad));

        // Then
        mockMvc.perform(
                        multipart(URL)
                                .file(inputZip)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                // then
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Failed to process ZIP: Error while creating TXT ZIP response"));

        // Verify
        verify(zipProcessingService).processZip(any());

    }

    /**
     * Helper: read back the ZIP returned by the controller and map fileName -> content.
     */
    private Map<String, String> extractZipEntries(byte[] zipBytes) throws Exception {

        Map<String, String> result = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                result.put(entry.getName(), baos.toString(StandardCharsets.UTF_8));
            }
        }

        return result;

    }

}