package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ApplicationFieldDefinitionDTO;
import com.ApplyZap.Tracker.dto.ApplicationFieldTemplateDTO;
import com.ApplyZap.Tracker.model.ApplicationStatus;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationTemplateService {

    static final String APPLICATION_CUSTOM_FIELDS_KEY = "applicationCustomFields";

    private static final Set<String> ALLOWED_TYPES = Set.of("text", "boolean", "select");

    private static final Set<String> BUILTIN_KEYS = Set.of(
            "companyName",
            "roleName",
            "jobLink",
            "status",
            "dateOfApplication",
            "tailored",
            "referral",
            "jobDescription");

    @Autowired
    private userRepository userRepository;

    @Autowired
    private userService userService;

    public ApplicationFieldTemplateDTO getFieldTemplate() {
        User user = userService.getCurrentUser();
        return buildTemplate(readCustomFields(user.getTrackerConfig()));
    }

    @Transactional
    public ApplicationFieldTemplateDTO replaceFieldTemplate(List<ApplicationFieldDefinitionDTO> custom) {
        if (custom == null) {
            throw new IllegalArgumentException("Custom field list is required");
        }
        List<Map<String, Object>> stored = toStoredCustomFields(custom);

        User user = userService.getCurrentUser();
        Map<String, Object> config = user.getTrackerConfig();
        Map<String, Object> merged = config != null ? new HashMap<>(config) : new HashMap<>();
        merged.put(APPLICATION_CUSTOM_FIELDS_KEY, stored);
        user.setTrackerConfig(merged);
        userRepository.save(user);

        return buildTemplate(readCustomFields(merged));
    }

    private ApplicationFieldTemplateDTO buildTemplate(List<ApplicationFieldDefinitionDTO> custom) {
        return new ApplicationFieldTemplateDTO(defaultBuiltIns(), custom);
    }

    private List<ApplicationFieldDefinitionDTO> defaultBuiltIns() {
        List<String> statusOptions = Arrays.stream(ApplicationStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        List<ApplicationFieldDefinitionDTO> builtIn = new ArrayList<>();
        builtIn.add(field("companyName", "Company", "text", 0, true, true, null));
        builtIn.add(field("roleName", "Role", "text", 1, true, true, null));
        builtIn.add(field("jobLink", "Job Link", "url", 2, false, true, null));
        builtIn.add(field("status", "Status", "select", 3, true, true, statusOptions));
        builtIn.add(field("dateOfApplication", "Date", "date", 4, false, true, null));
        builtIn.add(field("tailored", "Tailored", "boolean", 5, false, true, null));
        builtIn.add(field("referral", "Referral", "boolean", 6, false, true, null));
        builtIn.add(field("jobDescription", "Job Description", "textarea", 7, false, true, null));
        return builtIn;
    }

    private ApplicationFieldDefinitionDTO field(
            String key,
            String label,
            String type,
            int order,
            boolean required,
            boolean locked,
            List<String> options) {
        return new ApplicationFieldDefinitionDTO(key, label, type, order, required, locked, options);
    }

    private List<Map<String, Object>> toStoredCustomFields(List<ApplicationFieldDefinitionDTO> fields) {
        Set<String> seenKeys = new HashSet<>();
        List<Map<String, Object>> stored = new ArrayList<>();

        for (ApplicationFieldDefinitionDTO field : fields) {
            if (field == null) {
                throw new IllegalArgumentException("Custom field entry cannot be null");
            }
            if (field.getKey() == null || field.getKey().isBlank()) {
                throw new IllegalArgumentException("Each custom field requires a key");
            }
            if (field.getLabel() == null || field.getLabel().isBlank()) {
                throw new IllegalArgumentException("Each custom field requires a label");
            }

            String key = field.getKey().trim();
            String label = field.getLabel().trim();
            String type = field.getType() != null ? field.getType().trim().toLowerCase(Locale.ROOT) : "";

            if (BUILTIN_KEYS.contains(key)) {
                throw new IllegalArgumentException("Custom field key collides with built-in: " + key);
            }
            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException("Duplicate custom field key: " + key);
            }
            if (!ALLOWED_TYPES.contains(type)) {
                throw new IllegalArgumentException(
                        "Custom field type must be one of: text, boolean, select");
            }

            List<String> options = null;
            if ("select".equals(type)) {
                if (field.getOptions() == null || field.getOptions().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Select field requires non-empty options: " + key);
                }
                options = field.getOptions().stream()
                        .map(option -> option != null ? option.trim() : "")
                        .filter(option -> !option.isEmpty())
                        .toList();
                if (options.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Select field requires non-empty options: " + key);
                }
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("key", key);
            entry.put("label", label);
            entry.put("type", type);
            entry.put("order", field.getOrder());
            entry.put("required", field.isRequired());
            entry.put("locked", false);
            if (options != null) {
                entry.put("options", options);
            }
            stored.add(entry);
        }
        return stored;
    }

    private List<ApplicationFieldDefinitionDTO> readCustomFields(Map<String, Object> trackerConfig) {
        if (trackerConfig == null || !trackerConfig.containsKey(APPLICATION_CUSTOM_FIELDS_KEY)) {
            return List.of();
        }
        Object raw = trackerConfig.get(APPLICATION_CUSTOM_FIELDS_KEY);
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }

        List<ApplicationFieldDefinitionDTO> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String key = stringValue(map.get("key"));
            String label = stringValue(map.get("label"));
            String type = stringValue(map.get("type"));
            if (key == null || label == null || type == null) {
                continue;
            }
            int order = numberValue(map.get("order"));
            boolean required = booleanValue(map.get("required"));
            List<String> options = readOptions(map.get("options"));
            result.add(new ApplicationFieldDefinitionDTO(
                    key, label, type, order, required, false, options));
        }
        return result;
    }

    private List<String> readOptions(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return null;
        }
        List<String> options = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                String value = item.toString().trim();
                if (!value.isEmpty()) {
                    options.add(value);
                }
            }
        }
        return options.isEmpty() ? null : options;
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private int numberValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return false;
    }
}
