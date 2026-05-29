package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupBoardDTO;
import com.ApplyZap.Tracker.dto.GroupJobCreateDTO;
import com.ApplyZap.Tracker.dto.GroupJobRowDTO;
import com.ApplyZap.Tracker.dto.GroupJobStatusCellDTO;
import com.ApplyZap.Tracker.dto.GroupJobStatusUpdateDTO;
import com.ApplyZap.Tracker.dto.GroupJobUpdateDTO;
import com.ApplyZap.Tracker.dto.GroupMemberDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.GroupJobCellStatus;
import com.ApplyZap.Tracker.model.GroupJobStatus;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupJobRepository;
import com.ApplyZap.Tracker.repository.GroupJobStatusRepository;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import com.ApplyZap.Tracker.util.GroupUrlNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupJobService {

    @Autowired
    private GroupJobRepository groupJobRepository;
    @Autowired
    private GroupJobStatusRepository groupJobStatusRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private userService userService;
    @Autowired
    private GroupService groupService;

    @Transactional
    public GroupJob createJob(Long groupId, GroupJobCreateDTO dto) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(group, currentUser);
        if (dto.getJobLink() == null || dto.getJobLink().isBlank()) {
            throw new IllegalArgumentException("Job link is required");
        }
        String normalized = GroupUrlNormalizer.normalize(dto.getJobLink());
        if (normalized == null) {
            throw new IllegalArgumentException("Invalid job URL");
        }
        GroupJob existing = groupJobRepository.findByGroupAndNormalizedUrl(group, normalized).orElse(null);
        if (existing != null) {
            return existing;
        }
        GroupJob job = new GroupJob();
        job.setGroup(group);
        job.setNormalizedUrl(normalized);
        job.setOriginalUrl(dto.getJobLink().trim());
        job.setCompanyName(dto.getCompanyName() != null ? dto.getCompanyName().trim() : null);
        job.setRoleName(dto.getRoleName() != null ? dto.getRoleName().trim() : null);
        job.setAddedBy(currentUser);
        job = groupJobRepository.save(job);
        List<GroupMember> members = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
        for (GroupMember m : members) {
            GroupJobStatus cell = new GroupJobStatus();
            cell.setGroupJob(job);
            cell.setMember(m);
            cell.setStatus(GroupJobCellStatus.NA);
            groupJobStatusRepository.save(cell);
        }
        return job;
    }

    public GroupBoardDTO getBoard(Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(group, currentUser);
        List<GroupMember> members = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
        List<GroupJob> jobs = groupJobRepository.findByGroupOrderByCreatedAtDesc(group);
        List<GroupMemberDTO> memberDTOs = members.stream()
                .map(m -> new GroupMemberDTO(m.getId(), m.getDisplayName(), m.getRole(), m.getUser().getId()))
                .collect(Collectors.toList());
        List<GroupJobRowDTO> jobRows = new ArrayList<>();
        for (GroupJob job : jobs) {
            List<GroupJobStatus> statusList = groupJobStatusRepository.findByGroupJob(job);
            Map<Long, GroupJobCellStatus> statusByMemberId = statusList.stream()
                    .collect(Collectors.toMap(s -> s.getMember().getId(), GroupJobStatus::getStatus));
            List<GroupJobStatusCellDTO> cells = members.stream()
                    .map(m -> new GroupJobStatusCellDTO(m.getId(), statusByMemberId.getOrDefault(m.getId(), GroupJobCellStatus.NA)))
                    .collect(Collectors.toList());
            Long addedByMemberId = members.stream()
                    .filter(m -> m.getUser().getId().equals(job.getAddedBy().getId()))
                    .findFirst()
                    .map(GroupMember::getId)
                    .orElse(null);
            jobRows.add(new GroupJobRowDTO(
                    job.getId(),
                    job.getNormalizedUrl(),
                    job.getOriginalUrl(),
                    job.getCompanyName(),
                    job.getRoleName(),
                    job.getDateAdded(),
                    job.getAddedBy().getId(),
                    addedByMemberId,
                    cells));
        }
        return new GroupBoardDTO(group.getId(), group.getName(), memberDTOs, jobRows);
    }

    @Transactional
    public void updateJob(Long groupId, Long jobId, GroupJobUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(group, currentUser);
        GroupJob job = groupJobRepository.findByIdAndGroup(jobId, group)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (dto.getJobLink() != null && !dto.getJobLink().isBlank()) {
            String normalized = GroupUrlNormalizer.normalize(dto.getJobLink());
            if (normalized != null) {
                job.setNormalizedUrl(normalized);
                job.setOriginalUrl(dto.getJobLink().trim());
            }
        }
        if (dto.getCompanyName() != null) job.setCompanyName(dto.getCompanyName().trim());
        if (dto.getRoleName() != null) job.setRoleName(dto.getRoleName().trim());
        groupJobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long groupId, Long jobId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(group, currentUser);
        GroupJob job = groupJobRepository.findByIdAndGroup(jobId, group)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        List<GroupJobStatus> statuses = groupJobStatusRepository.findByGroupJob(job);
        groupJobStatusRepository.deleteAll(statuses);
        groupJobRepository.delete(job);
    }

    @Transactional
    public void updateMyStatus(Long groupId, Long jobId, GroupJobStatusUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
        GroupJob job = groupJobRepository.findByIdAndGroup(jobId, group)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (dto.getStatus() == null) {
            return;
        }
        GroupJobStatus cell = groupJobStatusRepository.findByGroupJobAndMember(job, member)
                .orElseGet(() -> {
                    GroupJobStatus c = new GroupJobStatus();
                    c.setGroupJob(job);
                    c.setMember(member);
                    c.setStatus(GroupJobCellStatus.NA);
                    return groupJobStatusRepository.save(c);
                });
        cell.setStatus(dto.getStatus());
        groupJobStatusRepository.save(cell);
    }
}
