package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.exception.ProfileValidationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileIdAssignerTest {

    private static Map<String, Object> item(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> at(Map<String, Object> profile, String key, int index) {
        return (Map<String, Object>) ((List<Object>) profile.get(key)).get(index);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> highlight(Map<String, Object> parent, int index) {
        return (Map<String, Object>) ((List<Object>) parent.get("highlights")).get(index);
    }

    private static Map<String, Object> profile() {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("experiences", new ArrayList<>(List.of(
                item("title", "SWE", "highlights", new ArrayList<>(List.of(item("text", "Built X")))))));
        profile.put("projects", new ArrayList<>(List.of(
                item("name", "Tracker", "highlights", new ArrayList<>(List.of(item("text", "Shipped")))))));
        profile.put("education", new ArrayList<>(List.of(item("school", "State U"))));
        return profile;
    }

    @Test
    void newItems_getPrefixedIds() {
        Map<String, Object> profile = profile();

        ProfileIdAssigner.assignIds(profile, false);

        Map<String, Object> exp = at(profile, "experiences", 0);
        Map<String, Object> proj = at(profile, "projects", 0);
        assertTrue(((String) exp.get("id")).matches("exp_[0-9a-f]{8}"));
        assertTrue(((String) highlight(exp, 0).get("id")).matches("h_[0-9a-f]{8}"));
        assertTrue(((String) proj.get("id")).matches("proj_[0-9a-f]{8}"));
        assertTrue(((String) highlight(proj, 0).get("id")).matches("h_[0-9a-f]{8}"));
        assertTrue(((String) at(profile, "education", 0).get("id")).matches("edu_[0-9a-f]{8}"));
    }

    @Test
    void existingIds_unchangedAcrossSaves() {
        Map<String, Object> profile = profile();
        at(profile, "experiences", 0).put("id", "exp_keepme01");
        ProfileIdAssigner.assignIds(profile, false);
        String expId = (String) at(profile, "experiences", 0).get("id");
        String hId = (String) highlight(at(profile, "experiences", 0), 0).get("id");
        String eduId = (String) at(profile, "education", 0).get("id");

        ProfileIdAssigner.assignIds(profile, false);

        assertEquals("exp_keepme01", expId);
        assertEquals(expId, at(profile, "experiences", 0).get("id"));
        assertEquals(hId, highlight(at(profile, "experiences", 0), 0).get("id"));
        assertEquals(eduId, at(profile, "education", 0).get("id"));
    }

    @Test
    void idsAreUniqueAcrossProfile() {
        Map<String, Object> profile = profile();
        ProfileIdAssigner.assignIds(profile, false);

        Set<Object> ids = new HashSet<>();
        ids.add(at(profile, "experiences", 0).get("id"));
        ids.add(highlight(at(profile, "experiences", 0), 0).get("id"));
        ids.add(at(profile, "projects", 0).get("id"));
        ids.add(highlight(at(profile, "projects", 0), 0).get("id"));
        ids.add(at(profile, "education", 0).get("id"));
        assertEquals(5, ids.size());
    }

    @Test
    void duplicateId_strict_rejectedWithPath() {
        Map<String, Object> profile = profile();
        at(profile, "experiences", 0).put("id", "dup_1");
        at(profile, "projects", 0).put("id", "dup_1");

        ProfileValidationException ex = assertThrows(ProfileValidationException.class,
                () -> ProfileIdAssigner.assignIds(profile, true));

        assertEquals("projects[0].id", ex.getField());
    }

    @Test
    void duplicateId_lenient_firstKeepsIdAndDuplicateGetsNewOne() {
        Map<String, Object> profile = profile();
        at(profile, "experiences", 0).put("id", "dup_1");
        highlight(at(profile, "experiences", 0), 0).put("id", "dup_1");

        ProfileIdAssigner.assignIds(profile, false);

        assertEquals("dup_1", at(profile, "experiences", 0).get("id"));
        String reassigned = (String) highlight(at(profile, "experiences", 0), 0).get("id");
        assertNotEquals("dup_1", reassigned);
        assertTrue(reassigned.matches("h_[0-9a-f]{8}"));
    }
}
