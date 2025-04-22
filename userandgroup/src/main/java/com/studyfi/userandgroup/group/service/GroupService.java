package com.studyfi.userandgroup.group.service;

import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.group.model.Group;
import com.studyfi.userandgroup.group.repo.GroupRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepo groupRepo;  // Ensure final field is properly initialized
    private final ModelMapper modelMapper;

    @Autowired  // Use this annotation to tell Spring to inject dependencies
    public GroupService(GroupRepo groupRepo, ModelMapper modelMapper) {
        this.groupRepo = groupRepo;
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
        group.setDescription(groupDTO.getDescription());
        groupRepo.save(group);
        return modelMapper.map(group, GroupDTO.class);
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
}
