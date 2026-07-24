package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFieldDefinitionDTO {
    private String key;
    private String label;
    private String type;
    private int order;
    private boolean required;
    private boolean locked;
    private List<String> options;
}
