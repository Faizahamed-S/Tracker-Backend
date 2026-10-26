package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ProfileReadinessDTO;
import com.ApplyZap.Tracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileReadinessServiceTest {

    private final ProfileReadinessService service = new ProfileReadinessService();
    private User user;
    private Map<String, Object> profile;

    private static Map<String, Object> obj(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    private static List<Object> list(Object... items) {
        return new ArrayList<>(List.of(items));
    }

    @BeforeEach
    void completeProfile() {
        user = new User();
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail("ada@example.com");

        profile = obj(
                "contact", obj("phone", "+1 408 555 0100", "address", obj(
                        "street", "1 Main St", "city", "San Jose", "state", "CA",
                        "zip", "95112", "country", "USA")),
                "workAuthorization", obj("authorizedToWork", true, "needsSponsorship", false),
                "education", list(obj("school", "SJSU", "degree", "MS", "fieldOfStudy", "CS", "endDate", "2025-05")),
                "experiences", list(obj("title", "SWE", "company", "Acme", "startDate", "2023-01", "current", true,
                        "highlights", list(obj("text", "Cut build time", "metric", "40s -> 6s")))),
                "projects", list(obj("name", "ApplyZap", "highlights", list(obj("text", "Built tracker")))),
                "skills", list(obj("category", "Languages", "items", list("Java", "TypeScript"))));
        user.setProfileData(profile);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sub(String key) {
        return (Map<String, Object>) profile.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> first(String key) {
        return (Map<String, Object>) ((List<Object>) profile.get(key)).get(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> address() {
        return (Map<String, Object>) sub("contact").get("address");
    }

    @Test
    void completeProfile_isResumeAndFormsReady() {
        ProfileReadinessDTO result = service.evaluate(user);

        assertTrue(result.isResumeReady());
        assertTrue(result.isFormsReady());
        assertTrue(result.getMissing().getResume().isEmpty());
        assertTrue(result.getMissing().getForms().isEmpty());
    }

    @Test
    void emptyProfile_listsEveryResumeRule() {
        user.setProfileData(null);
        user.setFirstName(" ");

        ProfileReadinessDTO result = service.evaluate(user);

        assertFalse(result.isResumeReady());
        assertFalse(result.isFormsReady());
        assertEquals(List.of("firstName", "contact.phone", "contact.address.city", "contact.address.state",
                "contact.address.country", "education", "experienceOrProject", "skills"),
                result.getMissing().getResume());
        assertEquals(List.of("contact.address.street", "contact.address.zip",
                "workAuthorization.authorizedToWork", "workAuthorization.needsSponsorship"),
                result.getMissing().getForms());
    }

    @Test
    void incompleteEducation_reportsEducation() {
        first("education").remove("fieldOfStudy");

        assertEquals(List.of("education"), service.evaluate(user).getMissing().getResume());
    }

    @Test
    void experienceRules_eachReported() {
        Map<String, Object> exp = first("experiences");
        exp.remove("title");
        exp.remove("company");
        exp.remove("startDate");
        exp.remove("current");
        exp.put("highlights", list(obj("text", "hidden", "include", false)));

        assertEquals(List.of("experiences[0].title", "experiences[0].company", "experiences[0].startDate",
                "experiences[0].endDate", "experiences[0].highlights"),
                service.evaluate(user).getMissing().getResume());
    }

    @Test
    void endDateSatisfiesInsteadOfCurrent() {
        Map<String, Object> exp = first("experiences");
        exp.remove("current");
        exp.put("endDate", "2024-06");

        assertTrue(service.evaluate(user).isResumeReady());
    }

    @Test
    void projectRules_eachReported() {
        Map<String, Object> project = first("projects");
        project.remove("name");
        project.remove("highlights");

        assertEquals(List.of("projects[0].name", "projects[0].highlights"),
                service.evaluate(user).getMissing().getResume());
    }

    @Test
    void experienceOnly_passes() {
        profile.remove("projects");

        assertTrue(service.evaluate(user).isResumeReady());
    }

    @Test
    void projectOnly_passes() {
        profile.remove("experiences");

        assertTrue(service.evaluate(user).isResumeReady());
    }

    @Test
    void neitherExperienceNorProject_fails() {
        profile.remove("experiences");
        profile.put("projects", list());

        assertEquals(List.of("experienceOrProject"), service.evaluate(user).getMissing().getResume());
    }

    @Test
    void legacySkillStrings_doNotCount() {
        profile.put("skills", list("Java", "SQL"));

        assertEquals(List.of("skills"), service.evaluate(user).getMissing().getResume());
    }

    @Test
    void v1FreeTextDates_reportedAsFormat_andNotResumeReady() {
        Map<String, Object> exp = first("experiences");
        exp.put("startDate", "Jan 2023");
        exp.remove("current");
        exp.put("endDate", "Present");
        first("education").put("endDate", "May 2025");

        ProfileReadinessDTO result = service.evaluate(user);

        assertFalse(result.isResumeReady());
        assertFalse(result.isFormsReady());
        assertEquals(List.of("education[0].endDate (format)", "experiences[0].startDate (format)",
                "experiences[0].endDate (format)"), result.getMissing().getResume());
    }

    @Test
    void formsExtrasOnlyInFormsList() {
        address().remove("street");
        address().remove("zip");
        sub("workAuthorization").remove("needsSponsorship");

        ProfileReadinessDTO result = service.evaluate(user);

        assertTrue(result.isResumeReady());
        assertFalse(result.isFormsReady());
        assertTrue(result.getMissing().getResume().isEmpty());
        assertEquals(List.of("contact.address.street", "contact.address.zip",
                "workAuthorization.needsSponsorship"), result.getMissing().getForms());
    }

    @Test
    void falseWorkAuthorizationAnswers_countAsAnswered() {
        sub("workAuthorization").put("authorizedToWork", false);

        assertTrue(service.evaluate(user).isFormsReady());
    }
}
