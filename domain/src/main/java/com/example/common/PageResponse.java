package com.example.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    @JsonProperty("last")
    private boolean isLast;


    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .isLast(page.isLast())
                .build();
    }

    public static <S, T> PageResponse<T> from(PageResponse<S> serviceResponse, Function<S, T> mapper) {
        List<T> converted = serviceResponse.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PageResponse.<T>builder()
                .content(converted)
                .page(serviceResponse.getPage())
                .size(serviceResponse.getSize())
                .totalElements(serviceResponse.getTotalElements())
                .totalPages(serviceResponse.getTotalPages())
                .hasNext(serviceResponse.isHasNext())
                .isLast(serviceResponse.isLast())
                .build();
    }
}