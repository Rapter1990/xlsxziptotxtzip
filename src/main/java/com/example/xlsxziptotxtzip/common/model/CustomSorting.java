package com.example.xlsxziptotxtzip.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

/**
 * Encapsulates sorting configuration for paginated requests.
 * This class is typically used together with {@link org.springframework.data.domain.Pageable}
 * by converting its state into a {@link Sort} via {@link #toSort()}.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CustomSorting {

    private String sortBy;

    private String sortDirection;  // "ASC" or "DESC"

    /**
     * Converts this sorting configuration into a Spring Data {@link Sort} instance.
     * If {@link #sortBy} is {@code null} or blank, this method returns {@link Sort#unsorted()}.
     * Otherwise, it creates a {@link Sort} using the given property and
     * {@link Sort.Direction} determined by {@link #sortDirection}.
     *
     * @return a {@link Sort} based on {@link #sortBy} and {@link #sortDirection},
     * or {@link Sort#unsorted()} if no valid sort property is provided
     */
    public Sort toSort() {
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "DESC".equalsIgnoreCase(sortDirection)
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(dir, sortBy);
        }
        return Sort.unsorted();
    }

}
