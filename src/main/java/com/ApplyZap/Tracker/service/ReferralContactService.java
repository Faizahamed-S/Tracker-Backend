package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.AssociatedApplicationDTO;
import com.ApplyZap.Tracker.dto.ReferralContactCreateUpdateDTO;
import com.ApplyZap.Tracker.dto.ReferralContactDetailDTO;
import com.ApplyZap.Tracker.dto.ReferralContactListDTO;
import com.ApplyZap.Tracker.dto.ReferralCustomFieldDefinitionDTO;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ReferralContact;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import com.ApplyZap.Tracker.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReferralContactService {

    static final String REFERRAL_CUSTOM_FIELDS_KEY = "referralCustomFields";

    @Autowired
    private ReferralContactRepository referralContactRepository;

    @Autowired
    private boardRepository boardRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private userService userService;

    public List<ReferralContactListDTO> listContacts(String search) {
        User user = userService.getCurrentUser();
        List<ReferralContact> contacts;
        if (search != null && !search.isBlank()) {
            contacts = referralContactRepository.searchByUser(user, search.trim());
        } else {
            contacts = referralContactRepository.findByUserOrderByNameAsc(user);
        }
        return contacts.stream().map(this::toListDTO).toList();
    }

    @Transactional
    public ReferralContactDetailDTO createContact(ReferralContactCreateUpdateDTO dto) {
        validateName(dto.getName());
        User user = userService.getCurrentUser();
        ReferralContact contact = new ReferralContact();
        contact.setUser(user);
        applyFields(contact, dto);
        contact = referralContactRepository.save(contact);
        return toDetailDTO(contact, List.of());
    }

    public ReferralContactDetailDTO getContact(Long id) {
        User user = userService.getCurrentUser();
        ReferralContact contact = requireOwned(id, user);
        List<Application> apps = boardRepository.findByUserAndReferralContact_Id(
                user, id, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toDetailDTO(contact, apps.stream().map(this::toAssociatedApplication).toList());
    }

    @Transactional
    public ReferralContactDetailDTO updateContact(Long id, ReferralContactCreateUpdateDTO dto) {
        validateName(dto.getName());
        User user = userService.getCurrentUser();
        ReferralContact contact = requireOwned(id, user);
        applyFields(contact, dto);
        contact = referralContactRepository.save(contact);
        List<Application> apps = boardRepository.findByUserAndReferralContact_Id(
                user, id, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toDetailDTO(contact, apps.stream().map(this::toAssociatedApplication).toList());
    }

    @Transactional
    public void deleteContact(Long id) {
        User user = userService.getCurrentUser();
        ReferralContact contact = requireOwned(id, user);
        List<Application> linked = boardRepository.findByUserAndReferralContact_Id(user, id, Sort.unsorted());
        for (Application app : linked) {
            app.setReferralContact(null);
            app.setReferral(false);
        }
        boardRepository.saveAll(linked);
        referralContactRepository.delete(contact);
    }

    public List<ReferralCustomFieldDefinitionDTO> getFieldTemplate() {
        User user = userService.getCurrentUser();
        return readFieldTemplate(user.getTrackerConfig());
    }

    @Transactional
    public List<ReferralCustomFieldDefinitionDTO> replaceFieldTemplate(
            List<ReferralCustomFieldDefinitionDTO> fields) {
        if (fields == null) {
            throw new IllegalArgumentException("Field template is required");
        }
        User user = userService.getCurrentUser();
        Map<String, Object> config = user.getTrackerConfig();
        Map<String, Object> merged = config != null ? new HashMap<>(config) : new HashMap<>();
        merged.put(REFERRAL_CUSTOM_FIELDS_KEY, toStoredFieldTemplate(fields));
        user.setTrackerConfig(merged);
        userRepository.save(user);
        return fields;
    }

    private ReferralContact requireOwned(Long id, User user) {
        return referralContactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new SecurityException("Referral contact not found"));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    private void applyFields(ReferralContact contact, ReferralContactCreateUpdateDTO dto) {
        contact.setName(dto.getName().trim());
        contact.setCompanyName(dto.getCompanyName());
        contact.setMobile(dto.getMobile());
        contact.setEmail(dto.getEmail());
        contact.setLinkedinUrl(dto.getLinkedinUrl());
        contact.setNotes(dto.getNotes());
        contact.setCustomFields(dto.getCustomFields());
    }

    private ReferralContactListDTO toListDTO(ReferralContact contact) {
        return new ReferralContactListDTO(
                contact.getId(),
                contact.getName(),
                contact.getCompanyName(),
                contact.getEmail(),
                contact.getMobile(),
                contact.getLinkedinUrl());
    }

    private ReferralContactDetailDTO toDetailDTO(
            ReferralContact contact, List<AssociatedApplicationDTO> associatedApplications) {
        return new ReferralContactDetailDTO(
                contact.getId(),
                contact.getName(),
                contact.getCompanyName(),
                contact.getMobile(),
                contact.getEmail(),
                contact.getLinkedinUrl(),
                contact.getNotes(),
                contact.getCustomFields(),
                contact.getCreatedAt(),
                contact.getUpdatedAt(),
                associatedApplications);
    }

    private AssociatedApplicationDTO toAssociatedApplication(Application app) {
        return new AssociatedApplicationDTO(
                app.getId(),
                app.getCompanyName(),
                app.getRoleName(),
                app.getDateOfApplication(),
                app.getStatus());
    }

    @SuppressWarnings("unchecked")
    private List<ReferralCustomFieldDefinitionDTO> readFieldTemplate(Map<String, Object> trackerConfig) {
        if (trackerConfig == null || !trackerConfig.containsKey(REFERRAL_CUSTOM_FIELDS_KEY)) {
            return List.of();
        }
        Object raw = trackerConfig.get(REFERRAL_CUSTOM_FIELDS_KEY);
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<ReferralCustomFieldDefinitionDTO> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String key = stringValue(map.get("key"));
            String label = stringValue(map.get("label"));
            int order = numberValue(map.get("order"));
            if (key != null && label != null) {
                result.add(new ReferralCustomFieldDefinitionDTO(key, label, order));
            }
        }
        return result;
    }

    private List<Map<String, Object>> toStoredFieldTemplate(List<ReferralCustomFieldDefinitionDTO> fields) {
        List<Map<String, Object>> stored = new ArrayList<>();
        for (ReferralCustomFieldDefinitionDTO field : fields) {
            if (field.getKey() == null || field.getKey().isBlank()
                    || field.getLabel() == null || field.getLabel().isBlank()) {
                throw new IllegalArgumentException("Each custom field requires key and label");
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("key", field.getKey().trim());
            entry.put("label", field.getLabel().trim());
            entry.put("order", field.getOrder());
            stored.add(entry);
        }
        return stored;
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
}
