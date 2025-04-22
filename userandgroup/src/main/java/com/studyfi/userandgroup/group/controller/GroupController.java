package com.studyfi.userandgroup.group.controller;

import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    // Create a new group
    @PostMapping("/create")
    public GroupDTO createGroup(@RequestBody GroupDTO groupDTO) {
        return groupService.createGroup(groupDTO);
    }

    // Update an existing group
    @PutMapping("/update/{groupId}")
    public GroupDTO updateGroup(@PathVariable Integer groupId, @RequestBody GroupDTO groupDTO) {  // Changed Long to Integer
        return groupService.updateGroup(groupId, groupDTO);
    }

    // Get all groups
    @GetMapping("/all")
    public List<GroupDTO> getAllGroups() {
        return groupService.getAllGroups();
    }

    // Get a group by ID
    @GetMapping("/{groupId}")
    public GroupDTO getGroupById(@PathVariable Integer groupId) {  // Changed Long to Integer
        return groupService.getGroupById(groupId);
    }
}