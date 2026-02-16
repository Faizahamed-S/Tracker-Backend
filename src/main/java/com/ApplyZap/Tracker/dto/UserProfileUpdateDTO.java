package com.ApplyZap.Tracker.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserProfileUpdateDTO {

    private String firstName;
    private String lastName;
    private String timezone;
    private Map<String, Object> profileData;
    private Map<String, Object> trackerConfig;
}
