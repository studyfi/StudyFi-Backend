package com.studyfi.userandgroup.group.controller;

import com.studyfi.userandgroup.group.dto.GroupCountsDTO;
import com.studyfi.userandgroup.group.dto.GroupCountsDTO;
import com.studyfi.userandgroup.service.CloudinaryService;
import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.group.service.GroupService;
import com.studyfi.userandgroup.user.service.UserService;
import com.studyfi.userandgroup.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


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
    private WebClient.Builder webClientBuilder;

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

    @GetMapping("/{groupId}/counts")
    public ResponseEntity<GroupCountsDTO> getGroupCounts(@PathVariable Integer groupId) {
        // Fetch user count using existing logic
        Integer userCount = getGroupUserCount(groupId);

        // Fetch content count from content microservice
        Integer contentCount = webClientBuilder.build().get()
                .uri("http://contentandnews/api/v1/content/group/" + groupId + "/count")
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorReturn(0)
                .block();

        // Fetch news count from news microservice
        Integer newsCount = webClientBuilder.build().get().uri("http://contentandnews/api/v1/news/group/" + groupId + "/count")
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorReturn(0)
                .block();

        // Fetch post count from news microservice
        Integer chatCount = webClientBuilder.build().get().uri("http://contentandnews/api/v1/chats/group/" + groupId + "/count")
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorReturn(0)
                .block();

        // Create and return the combined counts
        return ResponseEntity.ok(new GroupCountsDTO(contentCount, newsCount, userCount, chatCount));
    }

    @GetMapping("/{groupId}/users")
    public List<UserDTO> getUsersByGroup(@PathVariable Integer groupId){
        return groupService.getUserDTOsByGroupId(groupId);
    }

    @GetMapping("/{groupId}/userids")
    public List<Integer> getUserIdsByGroup(@PathVariable Integer groupId){
        return userService.getUsersByGroupId(groupId);
    }

    @GetMapping("/{groupId}/count")
    public Integer getGroupUserCount(@PathVariable Integer groupId){
        List<Integer> userIds = userService.getUsersByGroupId(groupId);
        if (userIds == null) {
            return 0;
        } else {
            return userIds.size();
        }
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

    // Remove a user from a group
    @DeleteMapping("/remove/{groupId}/user/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Integer groupId, @PathVariable Integer userId) {
        groupService.removeUserFromGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

}