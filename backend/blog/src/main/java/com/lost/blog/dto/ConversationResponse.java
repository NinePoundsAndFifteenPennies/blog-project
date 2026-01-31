package com.lost.blog.dto;

import java.time.LocalDateTime;

/**
 * 会话列表项响应DTO
 * 用于显示私信列表中的每个对话
 */
public class ConversationResponse {
    private Long partnerId;
    private String partnerUsername;
    private String partnerNickname;
    private String partnerAvatarUrl;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private boolean lastMessageSentByMe;
    private long unreadCount;
    private boolean friend;

    // --- Getters and Setters ---

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerUsername() {
        return partnerUsername;
    }

    public void setPartnerUsername(String partnerUsername) {
        this.partnerUsername = partnerUsername;
    }

    public String getPartnerNickname() {
        return partnerNickname;
    }

    public void setPartnerNickname(String partnerNickname) {
        this.partnerNickname = partnerNickname;
    }

    public String getPartnerAvatarUrl() {
        return partnerAvatarUrl;
    }

    public void setPartnerAvatarUrl(String partnerAvatarUrl) {
        this.partnerAvatarUrl = partnerAvatarUrl;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isLastMessageSentByMe() {
        return lastMessageSentByMe;
    }

    public void setLastMessageSentByMe(boolean lastMessageSentByMe) {
        this.lastMessageSentByMe = lastMessageSentByMe;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }
}
