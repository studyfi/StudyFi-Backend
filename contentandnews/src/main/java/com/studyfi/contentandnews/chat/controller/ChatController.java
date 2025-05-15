package com.studyfi.contentandnews.chat.controller;

import com.studyfi.contentandnews.chat.dto.CommentDTO;
import com.studyfi.contentandnews.chat.dto.LikeDTO;
import com.studyfi.contentandnews.chat.dto.PostDTO;
import com.studyfi.contentandnews.chat.dto.PostLikesSummaryDTO;
import com.studyfi.contentandnews.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/groups/{groupId}/posts")
    public ResponseEntity<PostDTO> createPost(@PathVariable Integer groupId, @RequestBody PostDTO postDTO) {
        postDTO.setGroupId(groupId);
        PostDTO createdPost = chatService.createPost(postDTO);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}/posts")
    public ResponseEntity<List<PostDTO>> getPostsByGroupId(
            @PathVariable Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostDTO> posts = chatService.getPostsByGroupId(groupId, page, size);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Integer postId) {
        PostDTO postDTO = chatService.getPost(postId);
        return new ResponseEntity<>(postDTO, HttpStatus.OK);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(@PathVariable Integer postId, @RequestBody CommentDTO commentDTO) {
        commentDTO.setPostId(postId);
        CommentDTO createdComment = chatService.createComment(commentDTO);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Integer postId) {
        List<CommentDTO> comments = chatService.getCommentsByPostId(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable Integer commentId) {
        CommentDTO comment = chatService.getComment(commentId);
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<LikeDTO> likePost(@PathVariable Integer postId, @RequestBody LikeDTO likeDTO) {
        likeDTO.setPostId(postId);
        LikeDTO like = chatService.likePost(likeDTO);
        return new ResponseEntity<>(like, HttpStatus.CREATED);
    }

    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<PostLikesSummaryDTO> getLikesFromPost(@PathVariable Integer postId, @RequestParam Integer currentUserId) {
        PostLikesSummaryDTO likes = chatService.getLikesFromPost(postId, currentUserId);
        return new ResponseEntity<>(likes, HttpStatus.OK);
    }

    @GetMapping("/group/{groupId}/count")
    public ResponseEntity<Integer> getPostCountByGroupId(@PathVariable Integer groupId) {
        int count = chatService.getPostCountByGroupId(groupId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
