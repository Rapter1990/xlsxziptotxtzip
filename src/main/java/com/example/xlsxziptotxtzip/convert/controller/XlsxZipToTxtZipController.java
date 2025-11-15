package com.example.xlsxziptotxtzip.convert.controller;

import com.example.xlsxziptotxtzip.convert.exception.InvalidZipContentException;
import com.example.xlsxziptotxtzip.convert.exception.ZipProcessingException;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.service.ZipProcessingService;
import com.example.xlsxziptotxtzip.convert.utils.FileNameUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * REST controller that exposes an endpoint to convert XLSX files inside a ZIP
 * into aligned TXT files and return them as a single ZIP archive.
 * <p>
 * High-level flow:
 * <ol>
 *     <li>Clients upload a ZIP file via {@link #uploadZip(MultipartFile)}.</li>
 *     <li>{@link ZipProcessingService} extracts and converts each {@code .xlsx} entry to text.</li>
 *     <li>The controller repackages all generated TXT contents into a new ZIP and
 *         returns it as {@code application/zip}.</li>
 * </ol>
 * If the uploaded ZIP is empty or contains no XLSX files,
 * an {@link InvalidZipContentException} is thrown. Errors during ZIP creation
 * are wrapped in a {@link ZipProcessingException}.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "XLSX ZIP to TXT ZIP", description = "Upload a ZIP of Mockaroo XLSX files and download aligned TXT files")
public class XlsxZipToTxtZipController {

    private final ZipProcessingService zipProcessingService;

    /**
     * Upload a ZIP containing one or more XLSX files, convert each XLSX to an aligned TXT file,
     * and return a new ZIP with the TXT files.
     *
     * @param file the uploaded ZIP file containing one or more {@code .xlsx} files
     * @return an {@link org.springframework.http.ResponseEntity} with
     * {@link Resource} body holding the generated ZIP of TXT files
     * @throws InvalidZipContentException if the file is empty or has no XLSX entries
     * @throws ZipProcessingException     if an error occurs while creating the output ZIP
     */
    @PostMapping(
            value = "/upload-zip",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/zip"   // explicitly say it's a ZIP
    )
    @Operation(
            summary = "Upload ZIP of XLSX files",
            description = "Takes a ZIP that contains one or more .xlsx files (e.g., from Mockaroo)," +
                    " converts each to a column-aligned .txt file, and returns a new ZIP of the .txt files."
    )
    public ResponseEntity<Resource> uploadZip(
            @Parameter(description = "ZIP file containing one or more .xlsx files", required = true)
            @RequestPart("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new InvalidZipContentException("Uploaded file is empty.");
        }

        List<ConvertedFile> convertedFiles = zipProcessingService.processZip(file);

        if (convertedFiles.isEmpty()) {
            throw new InvalidZipContentException("No XLSX files were found in the uploaded ZIP.");
        }

        // Build output ZIP of TXT files
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            for (ConvertedFile convertedFile : convertedFiles) {
                ZipEntry zipEntry = new ZipEntry(convertedFile.getTxtFileName());
                zos.putNextEntry(zipEntry);
                zos.write(convertedFile.getContent().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        } catch (Exception e) {
            throw new ZipProcessingException("Error while creating TXT ZIP response", e);
        }

        byte[] zipBytes = baos.toByteArray();
        String fileName = FileNameUtil.buildConvertedTxtZipFileName();

        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString()
                )
                .contentLength(zipBytes.length)
                .body(resource);
    }

}
