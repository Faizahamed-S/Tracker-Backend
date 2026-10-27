package com.ApplyZap.Tracker.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Merges an incoming profileData payload into the stored one by top-level key:
 * a key with a value replaces the stored value (arrays/objects replaced whole),
 * a key with null removes it, and absent or unknown keys are left as stored.
 */
public final class ProfileDataMerger {

    private ProfileDataMerger() {
    }

    public static Map<String, Object> merge(Map<String, Object> stored, Map<String, Object> incoming) {
        Map<String, Object> merged = stored != null ? new LinkedHashMap<>(stored) : new LinkedHashMap<>();
        if (incoming == null) {
            return merged;
        }
        for (Map.Entry<String, Object> entry : incoming.entrySet()) {
            if (entry.getValue() == null) {
                merged.remove(entry.getKey());
            } else {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }
}
