package com.example.xlsxziptotxtzip.convert.model.mapper;

import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFile;
import com.example.xlsxziptotxtzip.convert.model.dto.ConvertedFileSource;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConvertedFileSourceToConvertedFileMapperTest {

    private final ConvertedFileSourceToConvertedFileMapper mapper =
            Mappers.getMapper(ConvertedFileSourceToConvertedFileMapper.class);
    // or: ConvertedFileSourceToConvertedFileMapper.initialize();

    // ---------- mapFromSource (default method) tests ----------

    @Test
    void mapFromSource_whenSourceIsNull_returnsNull() {
        // given
        ConvertedFileSource source = null;

        // when
        ConvertedFile result = mapper.mapFromSource(source);

        // then
        assertNull(result);
    }

    @Test
    void mapFromSource_whenNameEndsWithLowercaseXlsx_replacesWithTxt_andCopiesContent() {
        // given
        ConvertedFileSource source = new ConvertedFileSource(
                "data.xlsx",
                "sample content"
        );

        // when
        ConvertedFile result = mapper.mapFromSource(source);

        // then
        assertNotNull(result);
        assertEquals("data.xlsx", result.getOriginalFileName());
        assertEquals("data.txt", result.getTxtFileName());   // extension replaced
        assertEquals("sample content", result.getContent());
    }

    @Test
    void mapFromSource_whenNameEndsWithUppercaseXLSX_replacesWithTxt_caseInsensitive() {
        // given
        ConvertedFileSource source = new ConvertedFileSource(
                "REPORT.XLSX",
                "report text"
        );

        // when
        ConvertedFile result = mapper.mapFromSource(source);

        // then
        assertNotNull(result);
        assertEquals("REPORT.XLSX", result.getOriginalFileName());
        assertEquals("REPORT.txt", result.getTxtFileName()); // case-insensitive replace
        assertEquals("report text", result.getContent());
    }

    @Test
    void mapFromSource_whenNameHasNoXlsxExtension_keepsName_asTxtFileName() {
        // given
        ConvertedFileSource source = new ConvertedFileSource(
                "archive",
                "archived data"
        );

        // when
        ConvertedFile result = mapper.mapFromSource(source);

        // then
        assertNotNull(result);
        assertEquals("archive", result.getOriginalFileName());
        // replaceAll doesn't match → originalName is kept as-is
        assertEquals("archive", result.getTxtFileName());
        assertEquals("archived data", result.getContent());
    }

    @Test
    void mapFromSource_whenOriginalNameIsNull_usesUnknownTxtAsFallback() {
        // given
        ConvertedFileSource source = new ConvertedFileSource(
                null,
                "no name content"
        );

        // when
        ConvertedFile result = mapper.mapFromSource(source);

        // then
        assertNotNull(result);
        assertNull(result.getOriginalFileName());
        assertEquals("unknown.txt", result.getTxtFileName());
        assertEquals("no name content", result.getContent());
    }

    // ---------- generated map(...) tests ----------

    @Test
    void map_whenSourceIsNull_returnsNull() {
        // given
        ConvertedFileSource source = null;

        // when
        ConvertedFile result = mapper.map(source);

        // then
        assertNull(result);
    }

    @Test
    void map_whenSourceIsNotNull_setsOriginalFileName_asGeneratedImplDefines() {
        // given
        ConvertedFileSource source = new ConvertedFileSource(
                "raw.xlsx",
                "ignored in generated map"
        );

        // when
        ConvertedFile result = mapper.map(source);

        // then
        assertNotNull(result);
        // Generated impl only sets originalFileName:
        assertEquals("raw.xlsx", result.getOriginalFileName());
        assertNull(result.getTxtFileName());
        assertNull(result.getContent());
    }

    // ---------- generated map(Collection<...>) tests ----------

    @Test
    void mapCollection_whenSourcesIsNull_returnsNull() {
        // given
        List<ConvertedFileSource> sources = null;

        // when
        List<ConvertedFile> result = mapper.map(sources);

        // then
        assertNull(result);
    }

    @Test
    void mapCollection_whenListHasItems_mapsEachItemUsingGeneratedMapImpl() {
        // given
        List<ConvertedFileSource> sources = List.of(
                new ConvertedFileSource("a.xlsx", "ignored"),
                new ConvertedFileSource("b.xlsx", "ignored2")
        );

        // when
        List<ConvertedFile> result = mapper.map(sources);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());

        ConvertedFile c1 = result.get(0);
        ConvertedFile c2 = result.get(1);

        // generated impl only sets originalFileName
        assertEquals("a.xlsx", c1.getOriginalFileName());
        assertNull(c1.getTxtFileName());
        assertNull(c1.getContent());

        assertEquals("b.xlsx", c2.getOriginalFileName());
        assertNull(c2.getTxtFileName());
        assertNull(c2.getContent());
    }

}