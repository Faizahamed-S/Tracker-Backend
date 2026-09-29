package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class userServiceTrackerConfigTest {

    private static final String SUPABASE_ID = "sb-user-1";

    @Mock
    private userRepository userRepository;

    @InjectMocks
    private userService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setSupabaseUserId(SUPABASE_ID);
        when(userRepository.findBySupabaseUserId(SUPABASE_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Map<String, Object> column(String id, String title) {
        return Map.of("id", id, "title", title, "color", "gray");
    }

    private static UserProfileUpdateDTO columnsOnly(List<Map<String, Object>> columns) {
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        Map<String, Object> config = new HashMap<>();
        config.put("columns", columns);
        dto.setTrackerConfig(config);
        return dto;
    }

    @Test
    void columnsOnlySave_preservesFieldTemplates() {
        List<Map<String, Object>> appFields = List.of(Map.of("key", "salary", "label", "Salary", "type", "number"));
        List<Map<String, Object>> refFields = List.of(Map.of("key", "linkedin", "label", "LinkedIn", "type", "text"));
        Map<String, Object> stored = new HashMap<>();
        stored.put("columns", List.of(column("c1", "Wishlist")));
        stored.put("applicationCustomFields", appFields);
        stored.put("referralCustomFields", refFields);
        user.setTrackerConfig(stored);

        List<Map<String, Object>> newColumns = List.of(column("c1", "Wishlist"), column("c2", "Phone Screen"));
        User saved = userService.updateUserProfile(SUPABASE_ID, columnsOnly(newColumns));

        assertEquals(newColumns, saved.getTrackerConfig().get("columns"));
        assertEquals(appFields, saved.getTrackerConfig().get("applicationCustomFields"));
        assertEquals(refFields, saved.getTrackerConfig().get("referralCustomFields"));
    }

    @Test
    void customColumns_savedExactlyInOrder() {
        Map<String, Object> stored = new HashMap<>();
        stored.put("columns", List.of(column("c1", "Wishlist")));
        user.setTrackerConfig(stored);

        List<Map<String, Object>> newColumns = new ArrayList<>();
        String[] titles = { "Wishlist", "Applied", "Phone Screen", "OA", "Onsite", "Offer", "Rejected", "Ghosted" };
        for (int i = 0; i < titles.length; i++) {
            newColumns.add(column("c" + i, titles[i]));
        }

        User saved = userService.updateUserProfile(SUPABASE_ID, columnsOnly(newColumns));

        assertEquals(newColumns, saved.getTrackerConfig().get("columns"));
    }

    @Test
    void nullStoredConfig_containsIncomingKeys() {
        user.setTrackerConfig(null);
        List<Map<String, Object>> newColumns = List.of(column("c1", "Applied"));

        User saved = userService.updateUserProfile(SUPABASE_ID, columnsOnly(newColumns));

        assertEquals(Map.of("columns", newColumns), saved.getTrackerConfig());
    }

    @Test
    void nullIncomingConfig_leavesStoredConfigUntouched() {
        Map<String, Object> stored = new HashMap<>();
        stored.put("columns", List.of(column("c1", "Wishlist")));
        stored.put("applicationCustomFields", List.of());
        user.setTrackerConfig(stored);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFirstName("Faiz");
        User saved = userService.updateUserProfile(SUPABASE_ID, dto);

        assertSame(stored, saved.getTrackerConfig());
        assertTrue(saved.getTrackerConfig().containsKey("applicationCustomFields"));
        assertEquals("Faiz", saved.getFirstName());
    }
}
