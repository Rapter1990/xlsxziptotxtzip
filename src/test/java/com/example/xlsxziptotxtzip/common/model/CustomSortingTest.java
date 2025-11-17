package com.example.xlsxziptotxtzip.common.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class CustomSortingTest {

    @Test
    void toSort_whenSortByIsNull_returnsUnsorted() {

        // given
        CustomSorting sorting = CustomSorting.builder()
                .sortBy(null)
                .sortDirection("DESC")
                .build();

        // when
        Sort result = sorting.toSort();

        // then
        assertThat(result.isUnsorted()).isTrue();

    }

    @Test
    void toSort_whenSortByIsBlank_returnsUnsorted() {

        // given
        CustomSorting sorting = CustomSorting.builder()
                .sortBy("   ")
                .sortDirection("ASC")
                .build();

        // when
        Sort result = sorting.toSort();

        // then
        assertThat(result.isUnsorted()).isTrue();

    }

    @Test
    void toSort_whenSortByPresent_andDirectionDesc_returnsDescSort() {

        // given
        CustomSorting sorting = CustomSorting.builder()
                .sortBy("time")
                .sortDirection("DeSc") // case-insensitive check
                .build();

        // when
        Sort result = sorting.toSort();

        // then
        assertThat(result.isSorted()).isTrue();
        assertThat(result).isEqualTo(Sort.by(Sort.Direction.DESC, "time"));

    }

    @Test
    void toSort_whenSortByPresent_andDirectionAscOrOther_returnsAscSort() {

        // case 1: explicit ASC
        CustomSorting ascSorting = CustomSorting.builder()
                .sortBy("time")
                .sortDirection("ASC")
                .build();

        // when
        Sort ascResult = ascSorting.toSort();

        // then
        assertThat(ascResult.isSorted()).isTrue();
        assertThat(ascResult).isEqualTo(Sort.by(Sort.Direction.ASC, "time"));

        // case 2: null direction → defaults to ASC
        CustomSorting nullDirSorting = CustomSorting.builder()
                .sortBy("time")
                .sortDirection(null)
                .build();

        Sort nullDirResult = nullDirSorting.toSort();

        assertThat(nullDirResult.isSorted()).isTrue();
        assertThat(nullDirResult).isEqualTo(Sort.by(Sort.Direction.ASC, "time"));

        // case 3: unexpected direction → defaults to ASC
        CustomSorting otherDirSorting = CustomSorting.builder()
                .sortBy("time")
                .sortDirection("INVALID")
                .build();

        Sort otherDirResult = otherDirSorting.toSort();

        assertThat(otherDirResult.isSorted()).isTrue();
        assertThat(otherDirResult).isEqualTo(Sort.by(Sort.Direction.ASC, "time"));

    }

}