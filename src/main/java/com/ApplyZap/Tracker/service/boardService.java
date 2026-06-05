package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ApplicationCreateDTO;
import com.ApplyZap.Tracker.dto.ApplicationCreateResponseDTO;
import com.ApplyZap.Tracker.dto.GroupAddResultDTO;
import com.ApplyZap.Tracker.dto.GroupJobCreateDTO;
import com.ApplyZap.Tracker.model.ActivityType;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationActivityLog;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ApplicationActivityLogRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import com.ApplyZap.Tracker.util.ApplicationListSort;
import com.ApplyZap.Tracker.util.StatusNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;

@Service
public class boardService {

    @Autowired
    boardRepository repo;

    @Autowired
    userService userService;

    @Autowired
    ApplicationActivityLogRepository activityLogRepository;

    @Autowired
    GroupJobService groupJobService;

    /**
     * Get all applications for the currently authenticated user.
     * Only returns applications that belong to the current user.
     */
    public List<Application> getApplications() {
        return getApplications(null, null, null);
    }

    /**
     * List applications with optional sort and filters. Omit sort for legacy (unordered) behavior.
     */
    public List<Application> getApplications(String sortParam, Boolean referral, Boolean tailored) {
        ApplicationListSort.validateParamOrThrow(sortParam);
        User user = userService.getCurrentUser();
        Optional<ApplicationListSort> sortOpt = ApplicationListSort.fromParam(sortParam);
        boolean filterReferral = referral != null;
        boolean filterTailored = tailored != null;

        if (sortOpt.isEmpty()) {
            if (!filterReferral && !filterTailored) {
                return repo.findByUser(user);
            }
            if (filterReferral && filterTailored) {
                return repo.findByUserAndReferralAndTailored(user, referral, tailored);
            }
            if (filterReferral) {
                return repo.findByUserAndReferral(user, referral);
            }
            return repo.findByUserAndTailored(user, tailored);
        }

        Sort sort = sortOpt.get().toSort();
        if (!filterReferral && !filterTailored) {
            return repo.findByUser(user, sort);
        }
        if (filterReferral && filterTailored) {
            return repo.findByUserAndReferralAndTailored(user, referral, tailored, sort);
        }
        if (filterReferral) {
            return repo.findByUserAndReferral(user, referral, sort);
        }
        return repo.findByUserAndTailored(user, tailored, sort);
    }

    /**
     * Get application by ID for the currently authenticated user.
     * Returns empty if application doesn't exist OR doesn't belong to current user.
     * This ensures users can only access their own applications.
     */
    public Optional<Application> getApplicationById(Long id) {
        User currentUser = userService.getCurrentUser();
        return repo.findByIdAndUser(id, currentUser);
    }

    /**
     * Create a new application for the currently authenticated user.
     * Optionally mirrors job link, company, and role to collaborative group boards.
     */
    @Transactional
    public ApplicationCreateResponseDTO createApplication(ApplicationCreateDTO dto) {
        Application saved = saveNewApplication(mapDtoToApplication(dto));
        List<GroupAddResultDTO> groupResults = addToGroups(dto, saved);
        return new ApplicationCreateResponseDTO(saved, groupResults);
    }

    private Application mapDtoToApplication(ApplicationCreateDTO dto) {
        Application application = new Application();
        application.setCompanyName(dto.getCompanyName());
        application.setRoleName(dto.getRoleName());
        application.setDateOfApplication(dto.getDateOfApplication());
        application.setJobLink(dto.getJobLink());
        application.setTailored(dto.isTailored());
        application.setJobDescription(dto.getJobDescription());
        application.setReferral(dto.isReferral());
        application.setStatus(dto.getStatus());
        application.setApplicationMetadata(dto.getApplicationMetadata());
        return application;
    }

    private Application saveNewApplication(Application application) {
        User currentUser = userService.getCurrentUser();
        application.setUser(currentUser);
        application.setStatus(StatusNormalizer.normalize(application.getStatus()));
        LocalDateTime now = LocalDateTime.now();
        application.setCreatedAt(now);
        application.setStatusUpdatedAt(now);
        Application saved = repo.save(application);
        ApplicationActivityLog log = new ApplicationActivityLog();
        log.setUser(currentUser);
        log.setApplication(saved);
        log.setActivityType(ActivityType.CREATED);
        log.setPreviousStatus(null);
        log.setNewStatus(saved.getStatus());
        activityLogRepository.save(log);
        return saved;
    }

    private List<GroupAddResultDTO> addToGroups(ApplicationCreateDTO dto, Application saved) {
        if (dto.getGroupIds() == null || dto.getGroupIds().isEmpty()) {
            return List.of();
        }
        List<GroupAddResultDTO> results = new ArrayList<>();
        for (Long groupId : dto.getGroupIds()) {
            if (groupId == null) {
                continue;
            }
            results.add(addToGroup(groupId, saved));
        }
        return results;
    }

    private GroupAddResultDTO addToGroup(Long groupId, Application saved) {
        try {
            if (saved.getJobLink() == null || saved.getJobLink().isBlank()) {
                return new GroupAddResultDTO(groupId, false, null, "Job link is required to add to a group");
            }
            GroupJobCreateDTO jobDto = new GroupJobCreateDTO(
                    saved.getJobLink(),
                    saved.getCompanyName(),
                    saved.getRoleName());
            GroupJob job = groupJobService.createJob(groupId, jobDto);
            return new GroupAddResultDTO(groupId, true, job.getId(), null);
        } catch (Exception e) {
            return new GroupAddResultDTO(groupId, false, null, e.getMessage());
        }
    }

    /**
     * Update an application. The existing application must belong to the current
     * user.
     * Ownership is verified by getApplicationById() before calling this method.
     */
    public Application updateApplication(Application existing, Application newUpdate) {
        // Ensure user cannot be changed through update
        // (user is already set and verified via getApplicationById)
        String oldStatus = existing.getStatus();

        if (newUpdate.getCompanyName() != null)
            existing.setCompanyName(newUpdate.getCompanyName());
        if (newUpdate.getRoleName() != null)
            existing.setRoleName(newUpdate.getRoleName());
        if (newUpdate.getDateOfApplication() != null) {
            if (existing.getDateOfApplication() == null || 
                new Date().getTime() - existing.getDateOfApplication().getTime() >= 60 * 1000) {
                existing.setDateOfApplication(newUpdate.getDateOfApplication());
            }
        }
        if (newUpdate.getJobLink() != null)
            existing.setJobLink(newUpdate.getJobLink());
        if (newUpdate.getJobDescription() != null)
            existing.setJobDescription(newUpdate.getJobDescription());
        if (newUpdate.isReferral())
            existing.setReferral(true);
        if (newUpdate.isTailored())
            existing.setTailored(true);
        if (newUpdate.getStatus() != null)
            existing.setStatus(StatusNormalizer.normalize(newUpdate.getStatus()));
        if (newUpdate.getApplicationMetadata() != null)
            existing.setApplicationMetadata(newUpdate.getApplicationMetadata());

        Application saved = repo.save(existing);

        // Log STATUS_CHANGE for analytics when status actually changed
        String newStatus = saved.getStatus();
        if (!Objects.equals(oldStatus, newStatus)) {
            saved.setStatusUpdatedAt(LocalDateTime.now());
            saved = repo.save(saved);
            ApplicationActivityLog log = new ApplicationActivityLog();
            log.setUser(saved.getUser());
            log.setApplication(saved);
            log.setActivityType(ActivityType.STATUS_CHANGE);
            log.setPreviousStatus(oldStatus);
            log.setNewStatus(newStatus);
            activityLogRepository.save(log);
        }
        return saved;
    }

    /**
     * Delete an application by ID for the currently authenticated user.
     * Only deletes if the application belongs to the current user.
     * Removes related activity log rows first to satisfy FK constraint, then deletes the application.
     * Returns true if deleted, false if not found or doesn't belong to user.
     */
    @Transactional
    public boolean deleteApplication(Long id) {
        User currentUser = userService.getCurrentUser();
        Optional<Application> application = repo.findByIdAndUser(id, currentUser);
        if (application.isPresent()) {
            activityLogRepository.deleteByApplication_Id(application.get().getId());
            repo.delete(application.get());
            return true;
        }
        return false;
    }

    /**
     * Get applications by status for the currently authenticated user.
     * Only returns applications that belong to the current user and match the
     * status.
     */
    public List<Application> getApplicationByStatus(String status) {
        return getApplicationByStatus(status, null, null, null);
    }

    public List<Application> getApplicationByStatus(String status, String sortParam, Boolean referral, Boolean tailored) {
        ApplicationListSort.validateParamOrThrow(sortParam);
        User user = userService.getCurrentUser();
        Optional<ApplicationListSort> sortOpt = ApplicationListSort.fromParam(sortParam);
        boolean filterReferral = referral != null;
        boolean filterTailored = tailored != null;

        if (sortOpt.isEmpty() && !filterReferral && !filterTailored) {
            return repo.findByUserAndStatusIgnoreCase(user, status);
        }

        List<Application> all = getApplications(sortParam, referral, tailored);
        String normalized = StatusNormalizer.normalize(status);
        return all.stream()
                .filter(a -> a.getStatus() != null
                        && Objects.equals(StatusNormalizer.normalize(a.getStatus()), normalized))
                .toList();
    }

    /**
     * Get all unique statuses used by the currently authenticated user.
     * Returns a list of distinct status strings (excluding null values).
     * Useful for populating dynamic board columns or status filters.
     */
    public List<String> getUniqueStatuses() {
        User currentUser = userService.getCurrentUser();
        List<String> raw = repo.findDistinctStatusesByUser(currentUser);
        return raw.stream()
                .map(StatusNormalizer::normalize)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }
}
