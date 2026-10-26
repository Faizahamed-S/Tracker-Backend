package com.ApplyZap.Tracker.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProfileDataMergerTest {

    private static Map<String, Object> stored() {
        Map<String, Object> stored = new HashMap<>();
        stored.put("aboutMe", "Hi");
        stored.put("links", List.of(Map.of("label", "GitHub", "url", "https://github.com/x")));
        stored.put("story", Map.of("whyThisWork", "because"));
        stored.put("someFutureKey", Map.of("a", 1));
        return stored;
    }

    @Test
    void absentKey_isKept() {
        Map<String, Object> merged = ProfileDataMerger.merge(stored(), Map.of("aboutMe", "Hello"));

        assertEquals("Hello", merged.get("aboutMe"));
        assertEquals(stored().get("links"), merged.get("links"));
        assertEquals(stored().get("story"), merged.get("story"));
    }

    @Test
    void presentKey_isReplacedWhole() {
        List<Map<String, String>> newLinks = List.of(Map.of("label", "Site", "url", "https://x.dev"));
        Map<String, Object> merged = ProfileDataMerger.merge(stored(), Map.of("links", newLinks));

        assertEquals(newLinks, merged.get("links"));
    }

    @Test
    void nullKey_isRemoved() {
        Map<String, Object> incoming = new HashMap<>();
        incoming.put("story", null);

        Map<String, Object> merged = ProfileDataMerger.merge(stored(), incoming);

        assertFalse(merged.containsKey("story"));
        assertEquals("Hi", merged.get("aboutMe"));
    }

    @Test
    void unknownKeys_arePreserved() {
        Map<String, Object> merged = ProfileDataMerger.merge(stored(), Map.of("brandNewKey", "x"));

        assertEquals(Map.of("a", 1), merged.get("someFutureKey"));
        assertEquals("x", merged.get("brandNewKey"));
    }

    @Test
    void nullStored_returnsIncomingWithoutNulls() {
        Map<String, Object> incoming = new HashMap<>();
        incoming.put("aboutMe", "Hi");
        incoming.put("story", null);

        Map<String, Object> merged = ProfileDataMerger.merge(null, incoming);

        assertEquals(Map.of("aboutMe", "Hi"), merged);
    }
}
