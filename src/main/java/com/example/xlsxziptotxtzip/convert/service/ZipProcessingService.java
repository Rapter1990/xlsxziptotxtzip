package com.example.xlsxziptotxtzip.convert.service;

import com.example.xlsxziptotxtzip.convert.exception.ZipProcessingException;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFileSource;
import com.example.xlsxziptotxtzip.convert.model.mapper.ConvertedFileSourceToConvertedFileMapper;
import com.example.xlsxziptotxtzip.convert.utils.XlsxToTextUtil;
import com.example.xlsxziptotxtzip.convert.utils.ZipXlsxUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service responsible for processing uploaded ZIP archives containing XLSX files.
 * <p>
 * The typical flow is:
 * <ol>
 *     <li>Read the uploaded ZIP from a {@link MultipartFile}.</li>
 *     <li>Iterate over its entries using {@link ZipInputStream}.</li>
 *     <li>Filter out non-XLSX entries via {@link ZipXlsxUtil#isXlsxFile(ZipEntry)}.</li>
 *     <li>Convert each XLSX file to aligned text using
 *         {@link XlsxToTextUtil#convertXlsxToAlignedText(java.io.InputStream)}.</li>
 *     <li>Map each result to a {@link ConvertedFile} using
 *         {@link ConvertedFileSourceToConvertedFileMapper}.</li>
 * </ol>
 * Any low-level I/O error is wrapped in a {@link ZipProcessingException}.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ZipProcessingService {

    private final ConvertedFileSourceToConvertedFileMapper mapper =
            ConvertedFileSourceToConvertedFileMapper.initialize();

    /**
     * Processes the given ZIP file and converts all contained {@code .xlsx} entries
     * into {@link ConvertedFile} instances.
     * <p>
     * For each ZIP entry:
     * <ul>
     *     <li>Non-XLSX entries are skipped (see {@link ZipXlsxUtil#isXlsxFile(ZipEntry)}).</li>
     *     <li>The XLSX bytes are read with {@link ZipXlsxUtil#readEntryBytes(ZipInputStream)}.</li>
     *     <li>The content is converted to aligned text using
     *         {@link XlsxToTextUtil#convertXlsxToAlignedText(java.io.InputStream)}.</li>
     *     <li>A {@link ConvertedFileSource} is created and mapped to {@link ConvertedFile}.</li>
     * </ul>
     * In case of an {@link IOException} while reading the ZIP stream, a
     * {@link ZipProcessingException} is thrown.
     * </p>
     *
     * @param zipFile the uploaded ZIP file containing one or more XLSX files
     * @return a {@link List} of {@link ConvertedFile} representing the converted TXT contents
     * @throws ZipProcessingException if an I/O error occurs while reading the ZIP
     */
    public List<ConvertedFile> processZip(MultipartFile zipFile) {
        List<ConvertedFile> result = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                // Skip non-xlsx entries
                if (!ZipXlsxUtil.isXlsxFile(entry)) {
                    zis.closeEntry();
                    continue;
                }

                // Read XLSX file content
                byte[] xlsxBytes = ZipXlsxUtil.readEntryBytes(zis);

                // Convert XLSX to aligned TXT
                String txtContent = XlsxToTextUtil.convertXlsxToAlignedText(
                        new ByteArrayInputStream(xlsxBytes)
                );

                // Build domain objects
                String entryName = entry.getName();
                ConvertedFileSource source = new ConvertedFileSource(entryName, txtContent);
                ConvertedFile convertedFile = mapper.mapFromSource(source);
                result.add(convertedFile);

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new ZipProcessingException("I/O error while reading uploaded ZIP", e);
        }

        return result;
    }

}