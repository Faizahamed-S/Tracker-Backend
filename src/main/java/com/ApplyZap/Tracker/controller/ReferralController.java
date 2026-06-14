package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.ReferralContactCreateUpdateDTO;
import com.ApplyZap.Tracker.dto.ReferralContactDetailDTO;
import com.ApplyZap.Tracker.dto.ReferralContactListDTO;
import com.ApplyZap.Tracker.dto.ReferralCustomFieldDefinitionDTO;
import com.ApplyZap.Tracker.dto.ReferralFieldTemplateDTO;
import com.ApplyZap.Tracker.service.ReferralContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/referrals")
@Tag(name = "Referral Base", description = "CRM contacts and custom field templates")
public class ReferralController {

    @Autowired
    private ReferralContactService referralContactService;

    @Operation(summary = "List referral contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Directory list"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping
    public ResponseEntity<List<ReferralContactListDTO>> listContacts(
            @Parameter(description = "Case-insensitive search on name or company")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(referralContactService.listContacts(search));
    }

    @Operation(summary = "Create referral contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contact created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<ReferralContactDetailDTO> createContact(@RequestBody ReferralContactCreateUpdateDTO dto) {
        ReferralContactDetailDTO created = referralContactService.createContact(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get custom field template")
    @GetMapping("/field-template")
    public ResponseEntity<ReferralFieldTemplateDTO> getFieldTemplate() {
        List<ReferralCustomFieldDefinitionDTO> fields = referralContactService.getFieldTemplate();
        return ResponseEntity.ok(new ReferralFieldTemplateDTO(fields));
    }

    @Operation(summary = "Replace custom field template")
    @PutMapping("/field-template")
    public ResponseEntity<ReferralFieldTemplateDTO> replaceFieldTemplate(
            @RequestBody ReferralFieldTemplateDTO dto) {
        List<ReferralCustomFieldDefinitionDTO> fields = referralContactService.replaceFieldTemplate(dto.getFields());
        return ResponseEntity.ok(new ReferralFieldTemplateDTO(fields));
    }

    @Operation(summary = "Get referral contact detail")
    @GetMapping("/{id}")
    public ResponseEntity<ReferralContactDetailDTO> getContact(@PathVariable Long id) {
        return ResponseEntity.ok(referralContactService.getContact(id));
    }

    @Operation(summary = "Update referral contact")
    @PutMapping("/{id}")
    public ResponseEntity<ReferralContactDetailDTO> updateContact(
            @PathVariable Long id,
            @RequestBody ReferralContactCreateUpdateDTO dto) {
        return ResponseEntity.ok(referralContactService.updateContact(id, dto));
    }

    @Operation(summary = "Delete referral contact")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        referralContactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
