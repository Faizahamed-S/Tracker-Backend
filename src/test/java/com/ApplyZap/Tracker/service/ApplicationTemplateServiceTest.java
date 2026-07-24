package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ApplicationFieldDefinitionDTO;
import com.ApplyZap.Tracker.dto.ApplicationFieldTemplateDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.userRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationTemplateServiceTest {

    @Mock
    private userRepository userRepository;
    @Mock
    private userService userService;

    @InjectMocks
    private ApplicationTemplateService applicationTemplateService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setTrackerConfig(new HashMap<>());
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getFieldTemplate_emptyCustom_returnsBuiltInsOnly() {
        ApplicationFieldTemplateDTO template = applicationTemplateService.getFieldTemplate();

        assertFalse(template.getBuiltIn().isEmpty());
        assertTrue(template.getCustom().isEmpty());
        assertTrue(template.getBuiltIn().stream().anyMatch(f -> "status".equals(f.getKey())));
        assertTrue(template.getBuiltIn().stream().allMatch(ApplicationFieldDefinitionDTO::isLocked));
    }

    @Test
    void replaceFieldTemplate_mergesAndPreservesOtherConfigKeys() {
        Map<String, Object> existing = new HashMap<>();
        existing.put("referralCustomFields", List.of(Map.of("key", "met_at", "label", "Met At")));
        user.setTrackerConfig(existing);

        List<ApplicationFieldDefinitionDTO> custom = List.of(
                new ApplicationFieldDefinitionDTO(
                        "external_id", "ID", "text", 0, false, false, null));

        ApplicationFieldTemplateDTO result = applicationTemplateService.replaceFieldTemplate(custom);

        assertEquals(1, result.getCustom().size());
        assertEquals("external_id", result.getCustom().get(0).getKey());
        assertFalse(result.getBuiltIn().isEmpty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Map<String, Object> config = userCaptor.getValue().getTrackerConfig();
        assertTrue(config.containsKey("referralCustomFields"));
        assertTrue(config.containsKey(ApplicationTemplateService.APPLICATION_CUSTOM_FIELDS_KEY));
    }

    @Test
    void replaceFieldTemplate_selectRequiresOptions() {
        List<ApplicationFieldDefinitionDTO> custom = List.of(
                new ApplicationFieldDefinitionDTO(
                        "priority", "Priority", "select", 0, false, false, List.of()));

        assertThrows(IllegalArgumentException.class,
                () -> applicationTemplateService.replaceFieldTemplate(custom));
    }

    @Test
    void replaceFieldTemplate_rejectsBuiltInKeyCollision() {
        List<ApplicationFieldDefinitionDTO> custom = List.of(
                new ApplicationFieldDefinitionDTO(
                        "status", "My Status", "text", 0, false, false, null));

        assertThrows(IllegalArgumentException.class,
                () -> applicationTemplateService.replaceFieldTemplate(custom));
    }

    @Test
    void replaceFieldTemplate_rejectsDuplicateKeys() {
        List<ApplicationFieldDefinitionDTO> custom = List.of(
                new ApplicationFieldDefinitionDTO("a", "A", "text", 0, false, false, null),
                new ApplicationFieldDefinitionDTO("a", "A2", "text", 1, false, false, null));

        assertThrows(IllegalArgumentException.class,
                () -> applicationTemplateService.replaceFieldTemplate(custom));
    }

    @Test
    void replaceFieldTemplate_rejectsInvalidType() {
        List<ApplicationFieldDefinitionDTO> custom = List.of(
                new ApplicationFieldDefinitionDTO(
                        "notes", "Notes", "textarea", 0, false, false, null));

        assertThrows(IllegalArgumentException.class,
                () -> applicationTemplateService.replaceFieldTemplate(custom));
    }

    @Test
    void getFieldTemplate_readsPersistedCustomFields() {
        user.setTrackerConfig(Map.of(
                ApplicationTemplateService.APPLICATION_CUSTOM_FIELDS_KEY,
                List.of(Map.of(
                        "key", "external_id",
                        "label", "ID",
                        "type", "text",
                        "order", 0,
                        "required", false,
                        "locked", false))));

        ApplicationFieldTemplateDTO template = applicationTemplateService.getFieldTemplate();

        assertEquals(1, template.getCustom().size());
        assertEquals("external_id", template.getCustom().get(0).getKey());
        assertEquals("ID", template.getCustom().get(0).getLabel());
    }

    @Test
    void replaceFieldTemplate_nullCustom_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> applicationTemplateService.replaceFieldTemplate(null));
    }
}
