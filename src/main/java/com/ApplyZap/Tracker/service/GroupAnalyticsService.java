package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupAnalyticsDTO;
import com.ApplyZap.Tracker.dto.GroupContributorDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupJobRepository;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupAnalyticsService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private GroupJobRepository groupJobRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private userService userService;
    @Autowired
    private GroupService groupService;

    public GroupAnalyticsDTO getAnalytics(Long groupId, LocalDate forDay) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(group, currentUser);
        if (forDay == null) {
            forDay = LocalDate.now();
        }
        long totalJobs = groupJobRepository.countByGroup(group);
        long jobsAddedToday = groupJobRepository.countByGroupAndCreatedAtToday(groupId);
        List<GroupJob> allJobs = groupJobRepository.findByGroupOrderByCreatedAtDesc(group);
        List<GroupMember> members = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
        Map<Long, Long> jobsAddedByUserId = allJobs.stream()
                .collect(Collectors.groupingBy(j -> j.getAddedBy().getId(), Collectors.counting()));
        List<GroupContributorDTO> contributorsOverall = members.stream()
                .map(m -> {
                    long count = jobsAddedByUserId.getOrDefault(m.getUser().getId(), 0L);
                    return new GroupContributorDTO(m.getId(), m.getUser().getId(), m.getDisplayName(), count);
                })
                .sorted(Comparator.comparingLong(GroupContributorDTO::getJobsAdded).reversed())
                .collect(Collectors.toList());
        GroupContributorDTO topOverall = contributorsOverall.isEmpty() ? null : contributorsOverall.get(0);

        LocalDateTime dayStart = forDay.atStartOfDay();
        LocalDateTime dayEnd = forDay.plusDays(1).atStartOfDay();
        List<GroupJob> jobsOnDay = allJobs.stream()
                .filter(j -> {
                    LocalDateTime t = j.getCreatedAt();
                    return !t.isBefore(dayStart) && t.isBefore(dayEnd);
                })
                .collect(Collectors.toList());
        Map<Long, Long> jobsAddedByUserIdOnDay = jobsOnDay.stream()
                .collect(Collectors.groupingBy(j -> j.getAddedBy().getId(), Collectors.counting()));
        List<GroupContributorDTO> contributorsByDay = members.stream()
                .map(m -> {
                    long count = jobsAddedByUserIdOnDay.getOrDefault(m.getUser().getId(), 0L);
                    return new GroupContributorDTO(m.getId(), m.getUser().getId(), m.getDisplayName(), count);
                })
                .sorted(Comparator.comparingLong(GroupContributorDTO::getJobsAdded).reversed())
                .collect(Collectors.toList());
        GroupContributorDTO topByDay = contributorsByDay.isEmpty() ? null : contributorsByDay.get(0);

        return new GroupAnalyticsDTO(
                totalJobs,
                jobsAddedToday,
                topOverall,
                contributorsOverall,
                topByDay,
                contributorsByDay,
                forDay.format(DATE_FORMAT));
    }
}
