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
}
