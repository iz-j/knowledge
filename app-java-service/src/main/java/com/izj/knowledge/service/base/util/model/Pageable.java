package com.izj.knowledge.service.base.util.model;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class Pageable<T, K> {
    @NonNull
    private final List<T> list;
    private final K nextKey;

    public <R> Pageable<R, K> map(Function<? super T, ? extends R> mapper) {
        return new Pageable<>(list.stream().map(mapper).collect(Collectors.toList()), nextKey);
    }
}
