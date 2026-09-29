package com.ApplyZap.Tracker.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads board column statuses from a user's trackerConfig ("columns" list), in
 * board order, normalized via StatusNormalizer and de-duplicated.
 * Column entries may be maps (title / name / label) or plain strings.
 */
public final class TrackerColumnStatuses {

    private TrackerColumnStatuses() {
    }

    public static List<String> fromTrackerConfig(Map<String, Object> trackerConfig) {
        if (trackerConfig == null || !trackerConfig.containsKey("columns")) {
            return List.of();
        }
        Object raw = trackerConfig.get("columns");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }

        Set<String> statuses = new LinkedHashSet<>();
        for (Object item : list) {
            String title = null;
            if (item instanceof Map<?, ?> map) {
                title = firstNonBlank(
                        stringValue(map.get("title")),
                        stringValue(map.get("name")),
                        stringValue(map.get("label")));
            } else if (item instanceof String s) {
                title = s;
            }
            String normalized = StatusNormalizer.normalize(title);
            if (normalized != null) {
                statuses.add(normalized);
            }
        }
        return new ArrayList<>(statuses);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
