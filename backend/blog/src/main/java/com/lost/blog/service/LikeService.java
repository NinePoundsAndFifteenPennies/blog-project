package com.lost.blog.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface LikeService {

    /**
     * Like a post
     * @param postId The ID of the post to like
     * @param currentUser The current authenticated user
     * @return The updated like count for the post
     */
    long likePost(Long postId, UserDetails currentUser);

    /**
     * Unlike a post
     * @param postId The ID of the post to unlike
     * @param currentUser The current authenticated user
     * @return The updated like count for the post
     */
    long unlikePost(Long postId, UserDetails currentUser);

    /**
     * Get the number of likes for a post
     * @param postId The ID of the post
     * @return The number of likes
     */
    long getLikeCount(Long postId);

    /**
     * Check if the current user has liked a post
     * @param postId The ID of the post
     * @param currentUser The current authenticated user (can be null for anonymous users)
     * @return true if the user has liked the post, false otherwise
     */
    boolean isPostLikedByUser(Long postId, UserDetails currentUser);

    /**
     * Like a comment
     * @param commentId The ID of the comment to like
     * @param currentUser The current authenticated user
     * @return The updated like count for the comment
     */
    long likeComment(Long commentId, UserDetails currentUser);

    /**
     * Unlike a comment
     * @param commentId The ID of the comment to unlike
     * @param currentUser The current authenticated user
     * @return The updated like count for the comment
     */
    long unlikeComment(Long commentId, UserDetails currentUser);

    /**
     * Get the number of likes for a comment
     * @param commentId The ID of the comment
     * @return The number of likes
     */
    long getCommentLikeCount(Long commentId);

    /**
     * Check if the current user has liked a comment
     * @param commentId The ID of the comment
     * @param currentUser The current authenticated user (can be null for anonymous users)
     * @return true if the user has liked the comment, false otherwise
     */
    boolean isCommentLikedByUser(Long commentId, UserDetails currentUser);
}
