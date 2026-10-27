package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileReadinessDTO {
    private boolean resumeReady;
    private boolean formsReady;
    private Missing missing;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Missing {
        /** Paths missing for a resume, e.g. "contact.phone", "experiences[0].startDate (format)". */
        private List<String> resume;
        /** Extra paths needed for application forms (beyond resume). */
        private List<String> forms;
    }
}
