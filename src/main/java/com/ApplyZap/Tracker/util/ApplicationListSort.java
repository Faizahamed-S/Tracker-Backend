package com.ApplyZap.Tracker.util;

import org.springframework.data.domain.Sort;

import java.util.Optional;

public enum ApplicationListSort {
    ADDED_DESC(Sort.by(Sort.Order.desc("createdAt").nullsLast())),
    ADDED_ASC(Sort.by(Sort.Order.asc("createdAt").nullsLast())),
    STATUS_UPDATED_DESC(Sort.by(Sort.Order.desc("statusUpdatedAt").nullsLast())),
    STATUS_UPDATED_ASC(Sort.by(Sort.Order.asc("statusUpdatedAt").nullsLast()));

    private final Sort sort;

    ApplicationListSort(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }

    public static Optional<ApplicationListSort> fromParam(String param) {
        if (param == null || param.isBlank()) {
            return Optional.empty();
        }
        return switch (param.trim().toLowerCase()) {
            case "added_desc" -> Optional.of(ADDED_DESC);
            case "added_asc" -> Optional.of(ADDED_ASC);
            case "status_updated_desc" -> Optional.of(STATUS_UPDATED_DESC);
            case "status_updated_asc" -> Optional.of(STATUS_UPDATED_ASC);
            default -> Optional.empty();
        };
    }

    public static void validateParamOrThrow(String param) {
        if (param != null && !param.isBlank() && fromParam(param).isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid sort. Allowed: added_desc, added_asc, status_updated_desc, status_updated_asc");
        }
    }
}
