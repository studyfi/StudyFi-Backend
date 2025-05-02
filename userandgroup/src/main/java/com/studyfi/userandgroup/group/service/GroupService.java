package com.studyfi.userandgroup.group.service;

import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.user.dto.UserDTO;
import com.studyfi.userandgroup.user.model.User;
import com.studyfi.userandgroup.group.model.Group;
import com.studyfi.userandgroup.group.repo.GroupRepo;
import com.studyfi.userandgroup.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GroupService {

    private final GroupRepo groupRepo;
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;

    @Autowired
    public GroupService(GroupRepo groupRepo, ModelMapper modelMapper, UserRepo userRepo) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
        this.modelMapper = modelMapper;

    }

    // Create a new group
    public GroupDTO createGroup(GroupDTO groupDTO) {
        Group group = modelMapper.map(groupDTO, Group.class);
        groupRepo.save(group);
        return modelMapper.map(group, GroupDTO.class);
    }

    // Update an existing group
    public GroupDTO updateGroup(Integer groupId, GroupDTO groupDTO) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        group.setName(groupDTO.getName());
        group.setImageUrl(groupDTO.getImageUrl());
        group.setDescription(groupDTO.getDescription());
        groupRepo.save(group);
        return modelMapper.map(group, GroupDTO.class);
    }

    // Validate group existence for a list of group IDs
    public void validateGroupExistence(List<Integer> groupIds) {
        for (Integer groupId : groupIds) {
            try{
                getGroupById(groupId);
            }catch (RuntimeException e){ throw e;}
        }
    }

    // Get all groups
    public List<GroupDTO> getAllGroups() {
        return groupRepo.findAll().stream()
                .map(group -> modelMapper.map(group, GroupDTO.class))
                .toList();
    }

    // Get a group by ID
    public GroupDTO getGroupById(Integer groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        return modelMapper.map(group, GroupDTO.class);
    }

    // Get groups for a user
    public List<GroupDTO> getGroupsByUser(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Group> groups = user.getGroups();
        return groups.stream().map(group -> modelMapper.map(group, GroupDTO.class)).collect(Collectors.toList());
    }

    // Get groups that a user has not joined
    public List<GroupDTO> getGroupsNotJoinedByUser(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Group> joinedGroups = user.getGroups();
        List<Group> allGroups = groupRepo.findAll();
        List<Group> notJoinedGroups = allGroups.stream().filter(group -> !joinedGroups.contains(group)).toList();
        return notJoinedGroups.stream().map(group -> modelMapper.map(group, GroupDTO.class)).collect(Collectors.toList());
    }

    public List<UserDTO> getUserDTOsByGroupId(Integer groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : group.getUsers()) {
            userDTOs.add(modelMapper.map(user,UserDTO.class));
        }
        return userDTOs;
    }
}
