package com.lost.blog.repository;

import com.lost.blog.model.Like;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Check if a user has liked a specific post
    boolean existsByUserAndPost(User user, Post post);
    
    // Find a like by user and post
    Optional<Like> findByUserAndPost(User user, Post post);
    
    // Count likes for a specific post
    long countByPost(Post post);
    
    // Delete a like by user and post
    void deleteByUserAndPost(User user, Post post);
    
    // Delete all likes for a specific post (for cascade deletion)
    void deleteByPost(Post post);
}
