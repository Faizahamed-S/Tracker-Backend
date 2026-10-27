package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.exception.ProfileValidationException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shape/type validation for profileData v2. Only keys present in the request are
 * checked; required-ness is never enforced here (see ProfileReadinessService).
 * Unknown keys, top-level or nested, are allowed.
 */
public final class ProfileDataValidator {

    public static final String SCHEMA_VERSION_KEY = "schemaVersion";

    private static final Pattern YEAR_MONTH = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    private static final Set<String> WORK_MODES = Set.of("remote", "hybrid", "onsite");

    private ProfileDataValidator() {
    }

    /** True when {@code value} is a "YYYY-MM" string. */
    public static boolean isYearMonth(Object value) {
        return value instanceof String s && YEAR_MONTH.matcher(s).matches();
    }

    /**
     * Interprets the request-level schemaVersion flag.
     *
     * @return true for 2 (strict v2 validation), false for absent/null or 1
     */
    public static boolean isStrictV2(Object schemaVersion) {
        if (schemaVersion == null) {
            return false;
        }
        if (schemaVersion instanceof Number n && n.doubleValue() == n.intValue()) {
            if (n.intValue() == 1) {
                return false;
            }
            if (n.intValue() == 2) {
                return true;
            }
        }
        throw new ProfileValidationException(SCHEMA_VERSION_KEY, "must be 1 or 2");
    }

    public static void validateV2(Map<String, Object> profileData) {
        if (profileData == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : profileData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            switch (key) {
                case "contact" -> validateContact(value, key);
                case "workAuthorization" -> validateWorkAuthorization(value, key);
                case "experiences" -> validateList(value, key, ProfileDataValidator::validateExperience);
                case "projects" -> validateList(value, key, ProfileDataValidator::validateProject);
                case "education" -> validateList(value, key, ProfileDataValidator::validateEducation);
                case "skills" -> validateList(value, key, ProfileDataValidator::validateSkillCategory);
                case "story" -> validateStory(value, key);
                case "preferences" -> validatePreferences(value, key);
                case "formDefaults" -> validateFormDefaults(value, key);
                default -> {
                }
            }
        }
    }

    private static void validateContact(Object value, String path) {
        Map<?, ?> contact = object(value, path);
        string(contact, "phone", path);
        Object address = contact.get("address");
        if (address != null) {
            String addressPath = path + ".address";
            Map<?, ?> addr = object(address, addressPath);
            for (String key : List.of("street", "city", "state", "zip", "country")) {
                string(addr, key, addressPath);
            }
        }
    }

    private static void validateWorkAuthorization(Object value, String path) {
        Map<?, ?> auth = object(value, path);
        bool(auth, "authorizedToWork", path);
        bool(auth, "needsSponsorship", path);
        string(auth, "visaType", path);
    }

    private static void validateExperience(Object value, String path) {
        Map<?, ?> exp = object(value, path);
        for (String key : List.of("id", "title", "company", "location", "description", "rawNotes")) {
            string(exp, key, path);
        }
        yearMonth(exp, "startDate", path);
        yearMonth(exp, "endDate", path);
        bool(exp, "current", path);
        highlights(exp, path);
    }

    private static void validateProject(Object value, String path) {
        Map<?, ?> project = object(value, path);
        for (String key : List.of("id", "name", "url", "rawNotes")) {
            string(project, key, path);
        }
        stringList(project, "techStack", path);
        yearMonth(project, "startDate", path);
        yearMonth(project, "endDate", path);
        highlights(project, path);
    }

    private static void validateEducation(Object value, String path) {
        Map<?, ?> edu = object(value, path);
        for (String key : List.of("id", "school", "degree", "fieldOfStudy", "location", "gpa")) {
            string(edu, key, path);
        }
        yearMonth(edu, "endDate", path);
    }

    private static void validateSkillCategory(Object value, String path) {
        Map<?, ?> category = object(value, path);
        string(category, "category", path);
        stringList(category, "items", path);
    }

    private static void validateStory(Object value, String path) {
        Map<?, ?> story = object(value, path);
        for (String key : List.of("whyThisWork", "proudProject", "lookingFor")) {
            string(story, key, path);
        }
        stringList(story, "voiceSamples", path);
    }

    private static void validatePreferences(Object value, String path) {
        Map<?, ?> prefs = object(value, path);
        for (String key : List.of("targetRoles", "locations", "avoidCompanies")) {
            stringList(prefs, key, path);
        }
        stringList(prefs, "workMode", path);
        Object workMode = prefs.get("workMode");
        if (workMode instanceof List<?> modes) {
            for (int i = 0; i < modes.size(); i++) {
                if (!WORK_MODES.contains(modes.get(i))) {
                    throw new ProfileValidationException(path + ".workMode[" + i + "]",
                            "must be one of remote, hybrid, onsite");
                }
            }
        }
        number(prefs, "salaryMin", path);
        number(prefs, "salaryMax", path);
    }

    private static void validateFormDefaults(Object value, String path) {
        Map<?, ?> defaults = object(value, path);
        string(defaults, "availableFrom", path);
        bool(defaults, "willingToRelocate", path);
        string(defaults, "howDidYouHear", path);
    }

    private static void highlights(Map<?, ?> parent, String parentPath) {
        Object value = parent.get("highlights");
        if (value == null) {
            return;
        }
        validateList(value, parentPath + ".highlights", (item, path) -> {
            Map<?, ?> highlight = object(item, path);
            string(highlight, "id", path);
            string(highlight, "text", path);
            stringList(highlight, "skills", path);
            string(highlight, "metric", path);
            bool(highlight, "include", path);
        });
    }

    private interface ItemValidator {
        void validate(Object item, String path);
    }

    private static void validateList(Object value, String path, ItemValidator itemValidator) {
        if (!(value instanceof List<?> list)) {
            throw new ProfileValidationException(path, "must be an array");
        }
        for (int i = 0; i < list.size(); i++) {
            itemValidator.validate(list.get(i), path + "[" + i + "]");
        }
    }

    private static Map<?, ?> object(Object value, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new ProfileValidationException(path, "must be an object");
        }
        return map;
    }

    private static void string(Map<?, ?> map, String key, String parentPath) {
        Object value = map.get(key);
        if (value != null && !(value instanceof String)) {
            throw new ProfileValidationException(parentPath + "." + key, "must be a string");
        }
    }

    private static void bool(Map<?, ?> map, String key, String parentPath) {
        Object value = map.get(key);
        if (value != null && !(value instanceof Boolean)) {
            throw new ProfileValidationException(parentPath + "." + key, "must be a boolean");
        }
    }

    private static void number(Map<?, ?> map, String key, String parentPath) {
        Object value = map.get(key);
        if (value != null && !(value instanceof Number)) {
            throw new ProfileValidationException(parentPath + "." + key, "must be a number");
        }
    }

    private static void stringList(Map<?, ?> map, String key, String parentPath) {
        Object value = map.get(key);
        if (value == null) {
            return;
        }
        String path = parentPath + "." + key;
        if (!(value instanceof List<?> list)) {
            throw new ProfileValidationException(path, "must be an array of strings");
        }
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof String)) {
                throw new ProfileValidationException(path + "[" + i + "]", "must be a string");
            }
        }
    }

    /** null or "" means not set; otherwise must be "YYYY-MM". */
    private static void yearMonth(Map<?, ?> map, String key, String parentPath) {
        Object value = map.get(key);
        if (value == null || "".equals(value)) {
            return;
        }
        if (!isYearMonth(value)) {
            throw new ProfileValidationException(parentPath + "." + key, "must be in YYYY-MM format");
        }
    }
}
