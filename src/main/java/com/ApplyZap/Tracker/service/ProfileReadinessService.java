package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ProfileReadinessDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.util.ProfileDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Single home for profile completeness rules (resume and application forms).
 * Dates that are set but not "YYYY-MM" are reported as "&lt;path&gt; (format)".
 */
@Service
public class ProfileReadinessService {

    private static final String FORMAT_SUFFIX = " (format)";

    @Autowired
    private userService userService;

    public ProfileReadinessDTO getForCurrentUser() {
        User current = userService.getCurrentUser();
        User user = userService.findBySupabaseUserId(current.getSupabaseUserId()).orElse(current);
        return evaluate(user);
    }

    public ProfileReadinessDTO evaluate(User user) {
        Map<?, ?> profile = user.getProfileData() != null ? user.getProfileData() : Map.of();
        List<String> resume = resumeMissing(user, profile);
        List<String> forms = formsMissing(profile);
        boolean resumeReady = resume.isEmpty();
        return new ProfileReadinessDTO(resumeReady, resumeReady && forms.isEmpty(),
                new ProfileReadinessDTO.Missing(resume, forms));
    }

    private List<String> resumeMissing(User user, Map<?, ?> profile) {
        List<String> missing = new ArrayList<>();
        if (isBlank(user.getFirstName())) missing.add("firstName");
        if (isBlank(user.getLastName())) missing.add("lastName");
        if (isBlank(user.getEmail())) missing.add("email");

        Map<?, ?> contact = map(profile.get("contact"));
        Map<?, ?> address = map(contact.get("address"));
        if (isBlank(contact.get("phone"))) missing.add("contact.phone");
        if (isBlank(address.get("city"))) missing.add("contact.address.city");
        if (isBlank(address.get("state"))) missing.add("contact.address.state");
        if (isBlank(address.get("country"))) missing.add("contact.address.country");

        checkEducation(list(profile.get("education")), missing);

        List<?> experiences = list(profile.get("experiences"));
        List<?> projects = list(profile.get("projects"));
        if (countObjects(experiences) == 0 && countObjects(projects) == 0) {
            missing.add("experienceOrProject");
        }
        checkExperiences(experiences, missing);
        checkProjects(projects, missing);

        if (!hasSkillCategoryWithItem(list(profile.get("skills")))) {
            missing.add("skills");
        }
        return missing;
    }

    private List<String> formsMissing(Map<?, ?> profile) {
        List<String> missing = new ArrayList<>();
        Map<?, ?> address = map(map(profile.get("contact")).get("address"));
        if (isBlank(address.get("street"))) missing.add("contact.address.street");
        if (isBlank(address.get("zip"))) missing.add("contact.address.zip");
        Map<?, ?> auth = map(profile.get("workAuthorization"));
        if (!(auth.get("authorizedToWork") instanceof Boolean)) missing.add("workAuthorization.authorizedToWork");
        if (!(auth.get("needsSponsorship") instanceof Boolean)) missing.add("workAuthorization.needsSponsorship");
        return missing;
    }

    private void checkEducation(List<?> education, List<String> missing) {
        boolean anyComplete = false;
        for (int i = 0; i < education.size(); i++) {
            if (!(education.get(i) instanceof Map<?, ?> edu)) continue;
            String path = "education[" + i + "]";
            checkDateFormat(edu, "endDate", path, missing);
            if (!isBlank(edu.get("school")) && !isBlank(edu.get("degree"))
                    && !isBlank(edu.get("fieldOfStudy")) && !isBlank(edu.get("endDate"))) {
                anyComplete = true;
            }
        }
        if (!anyComplete) missing.add("education");
    }

    private void checkExperiences(List<?> experiences, List<String> missing) {
        for (int i = 0; i < experiences.size(); i++) {
            if (!(experiences.get(i) instanceof Map<?, ?> exp)) continue;
            String path = "experiences[" + i + "]";
            if (isBlank(exp.get("title"))) missing.add(path + ".title");
            if (isBlank(exp.get("company"))) missing.add(path + ".company");
            if (isBlank(exp.get("startDate"))) missing.add(path + ".startDate");
            if (isBlank(exp.get("endDate")) && !Boolean.TRUE.equals(exp.get("current"))) {
                missing.add(path + ".endDate");
            }
            checkDateFormat(exp, "startDate", path, missing);
            checkDateFormat(exp, "endDate", path, missing);
            if (!hasIncludedHighlight(exp)) missing.add(path + ".highlights");
        }
    }

    private void checkProjects(List<?> projects, List<String> missing) {
        for (int i = 0; i < projects.size(); i++) {
            if (!(projects.get(i) instanceof Map<?, ?> project)) continue;
            String path = "projects[" + i + "]";
            if (isBlank(project.get("name"))) missing.add(path + ".name");
            checkDateFormat(project, "startDate", path, missing);
            checkDateFormat(project, "endDate", path, missing);
            if (!hasIncludedHighlight(project)) missing.add(path + ".highlights");
        }
    }

    private void checkDateFormat(Map<?, ?> item, String key, String path, List<String> missing) {
        Object value = item.get(key);
        if (!isBlank(value) && !ProfileDataValidator.isYearMonth(value)) {
            missing.add(path + "." + key + FORMAT_SUFFIX);
        }
    }

    /** A highlight counts when its text is non-blank and include is not false. */
    private boolean hasIncludedHighlight(Map<?, ?> parent) {
        for (Object item : list(parent.get("highlights"))) {
            if (item instanceof Map<?, ?> h && !isBlank(h.get("text")) && !Boolean.FALSE.equals(h.get("include"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSkillCategoryWithItem(List<?> skills) {
        for (Object item : skills) {
            if (item instanceof Map<?, ?> category && !isBlank(category.get("category"))) {
                for (Object skill : list(category.get("items"))) {
                    if (!isBlank(skill)) return true;
                }
            }
        }
        return false;
    }

    private static int countObjects(List<?> list) {
        int count = 0;
        for (Object item : list) {
            if (item instanceof Map) count++;
        }
        return count;
    }

    private static boolean isBlank(Object value) {
        return !(value instanceof String s) || s.isBlank();
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> m ? m : Map.of();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> l ? l : List.of();
    }
}
