package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.exception.ProfileValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDataValidatorTest {

    @SafeVarargs
    private static Map<String, Object> experiences(Map<String, Object>... items) {
        return Map.of("experiences", List.of(items));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2024-01", "1999-12", "2030-06" })
    void validYearMonth_passes(String date) {
        assertTrue(ProfileDataValidator.isYearMonth(date));
        assertDoesNotThrow(() -> ProfileDataValidator.validateV2(
                experiences(Map.of("title", "SWE", "startDate", date))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2024-13", "2024-00", "2024-1", "Jan 2023", "2024/01", "2024-01-15", "Present" })
    void invalidYearMonth_failsWithPath(String date) {
        assertFalse(ProfileDataValidator.isYearMonth(date));
        ProfileValidationException ex = assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(experiences(
                        Map.of("title", "A", "startDate", "2024-01"),
                        Map.of("title", "B", "startDate", date))));
        assertEquals("experiences[1].startDate", ex.getField());
    }

    @Test
    void emptyDate_isTreatedAsNotSet() {
        assertDoesNotThrow(() -> ProfileDataValidator.validateV2(
                experiences(Map.of("title", "SWE", "startDate", "", "current", true))));
    }

    @Test
    void educationAndProjectDates_validated() {
        ProfileValidationException edu = assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(Map.of("education",
                        List.of(Map.of("school", "U", "endDate", "May 2025")))));
        assertEquals("education[0].endDate", edu.getField());

        ProfileValidationException proj = assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(Map.of("projects",
                        List.of(Map.of("name", "P", "endDate", "2025")))));
        assertEquals("projects[0].endDate", proj.getField());
    }

    @Test
    void wrongTypes_rejected() {
        assertEquals("skills", assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(Map.of("skills", "java"))).getField());
        assertEquals("preferences.workMode[0]", assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(Map.of("preferences",
                        Map.of("workMode", List.of("office"))))).getField());
        assertEquals("workAuthorization.needsSponsorship", assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(Map.of("workAuthorization",
                        Map.of("needsSponsorship", "no")))).getField());
        assertEquals("experiences[0].highlights[0].include", assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.validateV2(experiences(Map.of("highlights",
                        List.of(Map.of("text", "x", "include", "yes")))))).getField());
    }

    @Test
    void partialProfile_withMissingFields_passes() {
        assertDoesNotThrow(() -> ProfileDataValidator.validateV2(Map.of(
                "contact", Map.of("address", Map.of("city", "SF")),
                "education", List.of(Map.of("school", "U")),
                "unknownKey", 42)));
    }

    @Test
    void schemaVersion_interpretation() {
        assertFalse(ProfileDataValidator.isStrictV2(null));
        assertFalse(ProfileDataValidator.isStrictV2(1));
        assertTrue(ProfileDataValidator.isStrictV2(2));
        assertEquals("schemaVersion", assertThrows(ProfileValidationException.class,
                () -> ProfileDataValidator.isStrictV2(3)).getField());
        assertThrows(ProfileValidationException.class, () -> ProfileDataValidator.isStrictV2("2"));
    }
}
