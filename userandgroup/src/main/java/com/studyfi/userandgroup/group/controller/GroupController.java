package com.studyfi.userandgroup.group.controller;

import com.studyfi.userandgroup.service.CloudinaryService;
import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.group.service.GroupService;
import com.studyfi.userandgroup.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;

@RestController
@ControllerAdvice
@CrossOrigin
@RequestMapping("/api/v1/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Create a new group
    @PostMapping(value = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GroupDTO createGroup(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName(name);
        groupDTO.setDescription(description);
        if (file != null) {
            String imageUrl = cloudinaryService.uploadFile(file);
            groupDTO.setImageUrl(imageUrl);
        }
        return groupService.createGroup(groupDTO);
    }

    // Update an existing group
    @PutMapping(value = "/update/{groupId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GroupDTO updateGroup(
            @PathVariable Integer groupId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName(name);
        groupDTO.setDescription(description);
        if (file != null) {
            String imageUrl = cloudinaryService.uploadFile(file);
            groupDTO.setImageUrl(imageUrl);
        }
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

    // Validate group existence
    @GetMapping("/validate")
    public ResponseEntity<?> validateGroups(@RequestParam List<Integer> groupIds) {
        try {
            groupService.validateGroupExistence(groupIds);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Invalid group IDs: " + e.getMessage());
        }
    }

    @GetMapping("/{groupId}/users")
    public List<Integer> getUsersByGroup(@PathVariable Integer groupId){
        return userService.getUsersByGroupId(groupId);
    }

    //Get groups for a user.
    @GetMapping("/user/{userId}")
    public List<GroupDTO> getGroupsByUser(@PathVariable Integer userId) {
        return groupService.getGroupsByUser(userId);
    }

    //Get groups that user has not joined
    @GetMapping("/notjoined/user/{userId}")
    public List<GroupDTO> getGroupsNotJoinedByUser(@PathVariable Integer userId){
        return groupService.getGroupsNotJoinedByUser(userId);
    }
}