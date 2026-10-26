package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.exception.ProfileValidationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Assigns stable ids to experiences, projects, education and highlights in a
 * (merged) profileData map. Existing ids are never changed, except a duplicate
 * on a non-strict (v1) save, where the first occurrence keeps the id and the
 * later one gets a new id. Ids are unique across the whole profile.
 */
public final class ProfileIdAssigner {

    private record Slot(Map<String, Object> item, String prefix, String path) {
    }

    private ProfileIdAssigner() {
    }

    /**
     * Fills in missing ids in place. Lists and items under the id-bearing keys are
     * replaced with copies so request/stored structures are not mutated.
     *
     * @param strict true for v2 requests: duplicate ids are rejected with 400
     */
    public static void assignIds(Map<String, Object> profileData, boolean strict) {
        if (profileData == null) {
            return;
        }
        List<Slot> slots = new ArrayList<>();
        copySection(profileData, "experiences", "exp", true, slots);
        copySection(profileData, "projects", "proj", true, slots);
        copySection(profileData, "education", "edu", false, slots);

        Set<String> seen = new HashSet<>();
        List<Slot> needsId = new ArrayList<>();
        for (Slot slot : slots) {
            Object id = slot.item().get("id");
            if (id instanceof String s && !s.isBlank()) {
                if (seen.add(s)) {
                    continue;
                }
                if (strict) {
                    throw new ProfileValidationException(slot.path() + ".id", "duplicate id " + s);
                }
            }
            needsId.add(slot);
        }

        for (Slot slot : needsId) {
            String id;
            do {
                id = slot.prefix() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            } while (!seen.add(id));
            slot.item().put("id", id);
        }
    }

    private static void copySection(Map<String, Object> profileData, String key, String prefix,
            boolean hasHighlights, List<Slot> slots) {
        if (!(profileData.get(key) instanceof List<?> list)) {
            return;
        }
        profileData.put(key, copyItems(list, prefix, key, hasHighlights, slots));
    }

    private static List<Object> copyItems(List<?> list, String prefix, String path,
            boolean hasHighlights, List<Slot> slots) {
        List<Object> copy = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof Map<?, ?> map)) {
                copy.add(item);
                continue;
            }
            String itemPath = path + "[" + i + "]";
            Map<String, Object> itemCopy = new LinkedHashMap<>();
            map.forEach((k, v) -> itemCopy.put(String.valueOf(k), v));
            slots.add(new Slot(itemCopy, prefix, itemPath));
            if (hasHighlights && itemCopy.get("highlights") instanceof List<?> highlights) {
                itemCopy.put("highlights", copyItems(highlights, "h", itemPath + ".highlights", false, slots));
            }
            copy.add(itemCopy);
        }
        return copy;
    }
}
