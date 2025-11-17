package com.example.xlsxziptotxtzip.common.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomPageTest {

    @Test
    void of_whenPageHasContent_mapsMetadataAndUsesProvidedDomainModels() {

        // Given
        List<String> pageContent = List.of("a", "b");
        Page<String> page = new PageImpl<>(
                pageContent,
                PageRequest.of(1, 10),
                42L
        );

        // Domain models of different type C (e.g. Integer)
        List<Integer> domainModels = List.of(100, 200);

        // when
        CustomPage<Integer> customPage = CustomPage.of(domainModels, page);

        // then
        assertNotNull(customPage);
        assertSame(domainModels, customPage.getContent());
        assertEquals(2, customPage.getPageNumber());
        assertEquals(10, customPage.getPageSize());
        assertEquals(42L, customPage.getTotalElementCount());
        assertEquals(page.getTotalPages(), customPage.getTotalPageCount());

    }

    @Test
    void of_whenPageIsEmpty_stillCarriesCorrectMetadataAndContent() {

        // Given
        Page<String> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 5), // first page (index 0)
                0L
        );

        List<String> domainModels = Collections.emptyList();

        // when
        CustomPage<String> customPage = CustomPage.of(domainModels, emptyPage);

        // Then
        assertNotNull(customPage);
        assertSame(domainModels, customPage.getContent());
        assertEquals(1, customPage.getPageNumber());
        assertEquals(5, customPage.getPageSize());
        assertEquals(0L, customPage.getTotalElementCount());
        assertEquals(0, customPage.getTotalPageCount());

    }

}