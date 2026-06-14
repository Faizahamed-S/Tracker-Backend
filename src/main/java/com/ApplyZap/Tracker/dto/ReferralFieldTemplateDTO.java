package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralFieldTemplateDTO {
    private List<ReferralCustomFieldDefinitionDTO> fields;
}
