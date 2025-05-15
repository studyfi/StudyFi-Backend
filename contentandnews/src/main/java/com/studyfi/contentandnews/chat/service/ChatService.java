package com.studyfi.contentandnews.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyfi.contentandnews.chat.dto.CommentDTO;
import com.studyfi.contentandnews.chat.dto.LikeDTO;
import com.studyfi.contentandnews.chat.dto.PostDTO;
import com.studyfi.contentandnews.chat.dto.PostLikesSummaryDTO;
import com.studyfi.contentandnews.chat.dto.UserDTO;
import com.studyfi.contentandnews.chat.model.Comment;
import com.studyfi.contentandnews.chat.model.Like;
import com.studyfi.contentandnews.chat.model.Post;
import com.studyfi.contentandnews.chat.repo.CommentRepo;
import com.studyfi.contentandnews.chat.repo.LikeRepo;
import com.studyfi.contentandnews.chat.repo.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;
    private final LikeRepo likeRepo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${userandgroup.base.url:http://userandgroup}")
    private String userandgroupBaseUrl;

    @Value("${notification.base.url:http://notification}")
    private String notificationBaseUrl;

    public ChatService(PostRepo postRepo, CommentRepo commentRepo, LikeRepo likeRepo, WebClient.Builder webClientBuilder) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.likeRepo = likeRepo;
        this.webClientBuilder = webClientBuilder;
    }

    public PostDTO createPost(PostDTO postDTO) {
        // Validate input
        if (postDTO == null || postDTO.getUser() == null || postDTO.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (postDTO.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        if (postDTO.getContent() == null || postDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be null or empty");
        }

        Post post = new Post();
        post.setGroupId(postDTO.getGroupId());
        post.setUserId(postDTO.getUser().getId());
        post.setContent(postDTO.getContent());
        post.setTimestamp(LocalDateTime.now());

        // Get user information from userandgroup microservice
        UserDTO userDTO = getUserDTOFromUserAndGroup(postDTO.getUser().getId());
        postDTO.setUser(userDTO);

        // Save the post
        Post savedPost = postRepo.save(post);
        postDTO.setPostId(savedPost.getPostId());
        postDTO.setTimestamp(savedPost.getTimestamp());
        postDTO.setLikeCount(0);
        postDTO.setCommentCount(0);

        // Send notification to all users in the group except the post author
        List<Integer> userIds = getUsersFromGroup(post.getGroupId()).stream()
                .filter(user -> user != null && user.getId() != null && !user.getId().equals(post.getUserId()))
                .map(UserDTO::getId)
                .toList();
        System.out.println("Users to notify: " + userIds);
        String message = String.format("New Post: by %s, in %s",
                postDTO.getUser().getName() != null ? postDTO.getUser().getName() : "Unknown User",
                getGroupName(postDTO.getGroupId()));
        sendNotificationToGroup(message, userIds);

        return postDTO;
    }

    public PostDTO getPost(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // Get user information from userandgroup microservice
        UserDTO userDTO = getUserDTOFromUserAndGroup(post.getUserId());

        PostDTO postDTO = new PostDTO();
        postDTO.setPostId(post.getPostId());
        postDTO.setGroupId(post.getGroupId());
        postDTO.setContent(post.getContent());
        postDTO.setTimestamp(post.getTimestamp());
        postDTO.setUser(userDTO);
        postDTO.setLikeCount(likeRepo.countByPostId(post.getPostId()));
        postDTO.setCommentCount(commentRepo.countByPostId(post.getPostId()));

        return postDTO;
    }

    public CommentDTO createComment(CommentDTO commentDTO) {
        // Validate input
        if (commentDTO == null || commentDTO.getUser() == null || commentDTO.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (commentDTO.getPostId() == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }

        Comment comment = new Comment();
        comment.setPostId(commentDTO.getPostId());
        comment.setUserId(commentDTO.getUser().getId());
        comment.setContent(commentDTO.getContent());
        comment.setTimestamp(LocalDateTime.now());

        // Get user information from userandgroup microservice
        UserDTO userDTO = getUserDTOFromUserAndGroup(commentDTO.getUser().getId());
        commentDTO.setUser(userDTO);

        Comment savedComment = commentRepo.save(comment);
        commentDTO.setCommentId(savedComment.getCommentId());
        commentDTO.setTimestamp(savedComment.getTimestamp());

        // Get post
        Post post = postRepo.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + comment.getPostId()));
        // Get all comments
        List<Comment> comments = commentRepo.findByPostId(comment.getPostId());
        // Get the user IDs that commented or the user of the post
        Set<Integer> userIds = new HashSet<>();
        userIds.add(post.getUserId());
        comments.forEach(c -> userIds.add(c.getUserId()));
        // Remove the user that created the comment
        userIds.remove(commentDTO.getUser().getId());
        String message = String.format("%s commented on a post in %s: %s",
                commentDTO.getUser().getName() != null ? commentDTO.getUser().getName() : "Unknown User",
                getGroupName(post.getGroupId()),
                commentDTO.getContent().substring(0, Math.min(commentDTO.getContent().length(), 20)));

        sendNotificationToGroup(message, userIds.stream().toList());
        return commentDTO;
    }

    public CommentDTO getComment(Integer commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        // Get user information from userandgroup microservice
        UserDTO userDTO = getUserDTOFromUserAndGroup(comment.getUserId());

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentId(comment.getCommentId());
        commentDTO.setPostId(comment.getPostId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setTimestamp(comment.getTimestamp());
        commentDTO.setUser(userDTO);

        return commentDTO;
    }

    public LikeDTO likePost(LikeDTO likeDTO) {
        if (likeDTO == null || likeDTO.getPostId() == null || likeDTO.getUserId() == null) {
            throw new IllegalArgumentException("Post ID and User ID cannot be null");
        }

        // Check if the user has already liked this post
        likeRepo.findByUserIdAndPostId(likeDTO.getUserId(), likeDTO.getPostId())
                .ifPresent(existingLike -> {
                    throw new IllegalArgumentException("User already liked this post.");
                });

        Like like = new Like();
        like.setPostId(likeDTO.getPostId());
        like.setUserId(likeDTO.getUserId());

        Like savedLike = likeRepo.save(like);
        likeDTO.setLikeId(savedLike.getLikeId());
        likeDTO.setLikedByUser(true); // The user who just liked it will have likedByUser set to true
        return likeDTO;
    }

    public PostLikesSummaryDTO getLikesFromPost(Integer postId, Integer currentUserId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        List<Like> likes = likeRepo.findByPostId(postId);

        List<Integer> likedUserIds = likes.stream()
                .map(Like::getUserId)
                .collect(Collectors.toList());

        List<UserDTO> likedUsers = likedUserIds.stream()
                .map(this::getUserDTOFromUserAndGroup)
                .collect(Collectors.toList());

        // Log values for debugging
        System.out.println("Post ID: " + postId);
        System.out.println("Current User ID: " + currentUserId);
        System.out.println("Liked User IDs: " + likedUserIds);
        boolean likedByCurrentUser = likedUserIds.contains(currentUserId);

        PostLikesSummaryDTO summaryDTO = new PostLikesSummaryDTO();
        summaryDTO.setLikeCount(likes.size());
        summaryDTO.setLikedUsers(likedUsers);
        summaryDTO.setLikedByCurrentUser(likedByCurrentUser);

        return summaryDTO;
    }

    public List<PostDTO> getPostsByGroupId(Integer groupId, int page, int size) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findByGroupId(groupId, pageable);

        return postPage.getContent().stream().map(post -> {
            UserDTO userDTO = getUserDTOFromUserAndGroup(post.getUserId());

            PostDTO postDTO = new PostDTO();
            postDTO.setPostId(post.getPostId());
            postDTO.setGroupId(post.getGroupId());
            postDTO.setContent(post.getContent());
            postDTO.setTimestamp(post.getTimestamp());
            postDTO.setUser(userDTO);
            postDTO.setLikeCount(likeRepo.countByPostId(post.getPostId()));
            postDTO.setCommentCount(commentRepo.countByPostId(post.getPostId()));
            return postDTO;
        }).collect(Collectors.toList());
    }

    private UserDTO getUserDTOFromUserAndGroup(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        try {
            String fullUrl = userandgroupBaseUrl + "/api/v1/users/" + userId;
            System.out.println("Calling userandgroup service: " + fullUrl);
            // Log raw JSON response for debugging
            String rawResponse = webClientBuilder.build().get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> System.err.println("WebClient error: " + e.getMessage()))
                    .block();
            System.out.println("Raw response: " + rawResponse);

            UserDTO userDTO = webClientBuilder.build().get()
                    .uri(fullUrl)
                    .retrieve()
                    .onStatus(status -> status.equals(HttpStatus.BAD_REQUEST) || status.equals(HttpStatus.NOT_FOUND),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalArgumentException("Invalid user ID: " + userId + ", response: " + body)))
                    .bodyToMono(UserDTO.class)
                    .block();
            System.out.println("Parsed UserDTO: " + userDTO);

            // Validate the returned UserDTO
            if (userDTO == null || userDTO.getId() == null || userDTO.getName() == null) {
                System.err.println("Invalid UserDTO received for userId " + userId + ": " + userDTO);
                throw new IllegalArgumentException("Invalid user data received for userId: " + userId);
            }
            return userDTO;
        } catch (WebClientResponseException e) {
            System.err.println("Error fetching user ID " + userId + ": " + e.getMessage());
            throw new IllegalArgumentException("Failed to fetch user data for userId: " + userId, e);
        } catch (Exception e) {
            System.err.println("Error fetching user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to fetch user data for userId: " + userId, e);
        }
    }

    private List<UserDTO> getUsersFromGroup(Integer groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        try {
            String fullUrl = userandgroupBaseUrl + "/api/v1/groups/" + groupId + "/users";
            System.out.println("Calling userandgroup service: " + fullUrl);
            // Log raw JSON response for debugging
            String rawResponse = webClientBuilder.build().get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> System.err.println("WebClient error: " + e.getMessage()))
                    .block();
            System.out.println("Raw response: " + rawResponse);

            List<UserDTO> users = webClientBuilder.build().get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToFlux(UserDTO.class)
                    .collectList()
                    .block();
            System.out.println("Parsed UserDTOs: " + users);

            // Filter out invalid UserDTOs
            if (users != null) {
                return users.stream()
                        .filter(user -> user != null && user.getId() != null && user.getName() != null)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error getting user information: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getGroupName(Integer groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        try {
            String fullUrl = userandgroupBaseUrl + "/api/v1/groups/" + groupId;
            System.out.println("Calling userandgroup service: " + fullUrl);
            String jsonResponse = webClientBuilder.build().get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse JSON to extract group name
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            String groupName = jsonNode.get("name").asText();

            return groupName != null ? groupName : "group";
        } catch (Exception e) {
            System.out.println("Error getting group name: " + e.getMessage());
            return "group";
        }
    }

    private void sendNotificationToGroup(String message, List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            System.out.println("No users to notify");
            return;
        }
        System.out.println("Users to notify: " + userIds);
        // Use the correct notification endpoint
        try {
            String fullUrl = notificationBaseUrl + "/api/v1/notifications/addnotification";
            System.out.println("Sending notification to: " + fullUrl);
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("groupIds", userIds); // Assuming userIds are treated as groupIds in notification service
            webClientBuilder.build().post()
                    .uri(fullUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.equals(HttpStatus.BAD_REQUEST) || status.equals(HttpStatus.NOT_FOUND),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalStateException("Failed to send notification, response: " + body)))
                    .bodyToMono(Void.class)
                    .block();
        } catch (IllegalStateException e) {
            System.out.println("Error sending notification: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error sending notification: " + e.getMessage());
        }
    }

    public List<CommentDTO> getCommentsByPostId(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        List<Comment> comments = commentRepo.findByPostId(postId);

        return comments.stream().map(comment -> {
            UserDTO userDTO = getUserDTOFromUserAndGroup(comment.getUserId());
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setCommentId(comment.getCommentId());
            commentDTO.setPostId(comment.getPostId());
            commentDTO.setContent(comment.getContent());
            commentDTO.setTimestamp(comment.getTimestamp());
            commentDTO.setUser(userDTO);
            return commentDTO;
        }).collect(Collectors.toList());
    }

    public int getPostCountByGroupId(Integer groupId) {
        return postRepo.countByGroupId(groupId);
    }
}