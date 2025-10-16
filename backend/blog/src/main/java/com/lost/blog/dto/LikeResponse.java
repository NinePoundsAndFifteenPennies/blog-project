package com.lost.blog.dto;

public class LikeResponse {
    private long likeCount;
    private boolean isLiked;

    public LikeResponse() {
    }

    public LikeResponse(long likeCount, boolean isLiked) {
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }

    // Getters and Setters
    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
