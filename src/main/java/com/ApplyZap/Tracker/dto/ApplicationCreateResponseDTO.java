package com.ApplyZap.Tracker.dto;

import com.ApplyZap.Tracker.model.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateResponseDTO {
    private Application application;
    private List<GroupAddResultDTO> groupResults;
}
