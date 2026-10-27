package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
import com.ApplyZap.Tracker.exception.ProfileValidationException;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.userRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class userServiceProfileDataTest {

    private static final String SUPABASE_ID = "sb-user-1";

    @Mock
    private userRepository userRepository;

    @InjectMocks
    private userService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setSupabaseUserId(SUPABASE_ID);
        when(userRepository.findBySupabaseUserId(SUPABASE_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Map<String, Object> obj(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    private User save(Map<String, Object> profileData) {
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setProfileData(profileData);
        return userService.updateUserProfile(SUPABASE_ID, dto);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstExperience(User user) {
        return (Map<String, Object>) ((List<Object>) user.getProfileData().get("experiences")).get(0);
    }

    @Test
    void v1Save_mergesAndKeepsV2Keys() {
        user.setProfileData(new HashMap<>(Map.of(
                "aboutMe", "old",
                "story", Map.of("whyThisWork", "mine"))));

        User saved = save(obj("aboutMe", "new", "links", List.of()));

        assertEquals("new", saved.getProfileData().get("aboutMe"));
        assertEquals(Map.of("whyThisWork", "mine"), saved.getProfileData().get("story"));
    }

    @Test
    void v1Save_acceptsFreeTextDates() {
        User saved = save(obj("experiences", List.of(obj("title", "SWE", "startDate", "Jan 2023"))));

        assertEquals("Jan 2023", firstExperience(saved).get("startDate"));
        assertTrue(((String) firstExperience(saved).get("id")).startsWith("exp_"));
    }

    @Test
    void v2Save_rejectsFreeTextDatesWithPath() {
        ProfileValidationException ex = assertThrows(ProfileValidationException.class,
                () -> save(obj("schemaVersion", 2,
                        "experiences", List.of(obj("title", "SWE", "startDate", "Jan 2023")))));

        assertEquals("experiences[0].startDate", ex.getField());
    }

    @Test
    void schemaVersion_neverStored_v2ThenV1() {
        save(obj("schemaVersion", 2, "aboutMe", "v2 save"));
        assertFalse(user.getProfileData().containsKey("schemaVersion"));

        User saved = save(obj("aboutMe", "v1 save"));

        assertFalse(saved.getProfileData().containsKey("schemaVersion"));
        assertEquals("v1 save", saved.getProfileData().get("aboutMe"));
    }

    @Test
    void storedSchemaVersion_isLeftAlone() {
        user.setProfileData(new HashMap<>(Map.of("schemaVersion", 2, "aboutMe", "x")));

        User saved = save(obj("aboutMe", "y"));

        assertEquals(2, saved.getProfileData().get("schemaVersion"));
    }

    @Test
    void existingIds_stableAcrossSaves() {
        save(obj("experiences", List.of(obj("title", "SWE"))));
        String id = (String) firstExperience(user).get("id");

        Map<String, Object> roundTrip = new LinkedHashMap<>(firstExperience(user));
        roundTrip.put("title", "Senior SWE");
        User saved = save(obj("experiences", new ArrayList<>(List.of(roundTrip))));

        assertEquals(id, firstExperience(saved).get("id"));
        assertEquals("Senior SWE", firstExperience(saved).get("title"));
    }

    @Test
    void duplicateIds_v2Rejected() {
        ProfileValidationException ex = assertThrows(ProfileValidationException.class,
                () -> save(obj("schemaVersion", 2, "experiences", List.of(
                        obj("id", "exp_same0001", "title", "A"),
                        obj("id", "exp_same0001", "title", "B")))));

        assertEquals("experiences[1].id", ex.getField());
    }

    @Test
    @SuppressWarnings("unchecked")
    void duplicateIds_v1Reassigned() {
        User saved = save(obj("experiences", List.of(
                obj("id", "exp_same0001", "title", "A"),
                obj("id", "exp_same0001", "title", "B"))));

        List<Map<String, Object>> exps = (List<Map<String, Object>>) saved.getProfileData().get("experiences");
        assertEquals("exp_same0001", exps.get(0).get("id"));
        assertNotEquals("exp_same0001", exps.get(1).get("id"));
        assertTrue(((String) exps.get(1).get("id")).startsWith("exp_"));
    }

    @Test
    void invalidSchemaVersion_rejected() {
        ProfileValidationException ex = assertThrows(ProfileValidationException.class,
                () -> save(obj("schemaVersion", 3)));

        assertEquals("schemaVersion", ex.getField());
    }
}
