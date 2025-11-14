package com.example.xlsxziptotxtzip.common.model.mapper;

import java.util.Collection;
import java.util.List;

/**
 * Generic interface for mapping between source and target types.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface BaseMapper<S, T> {

    /**
     * Maps a single source object to a target object.
     *
     * @param source the source instance to map (may be {@code null} depending on implementation)
     * @return the mapped target instance, or {@code null} if the implementation chooses to
     */
    T map(S source);

    /**
     * Maps a collection of source objects to a {@link List} of target objects.
     * Typical implementations will iterate over the given {@link Collection} and
     * delegate to {@link #map(Object)} for each element.
     *
     * @param sources the {@link Collection} of source instances to map
     * @return a {@link List} containing the mapped target instances,
     *         never {@code null} (implementations usually return an empty list for {@code null} or empty input)
     */
    List<T> map(Collection<S> sources);

}
