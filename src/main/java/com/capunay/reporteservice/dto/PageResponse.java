package com.capunay.reporteservice.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int offset,
        int limit,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> PageResponse<T> of(List<T> content, int offset, int limit, long totalElements) {
        int totalPages = limit > 0 ? (int) Math.ceil((double) totalElements / limit) : 0;
        boolean hasNext = offset + content.size() < totalElements;
        return new PageResponse<>(content, offset, limit, totalElements, totalPages, hasNext);
    }
}
