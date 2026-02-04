package com.lost.blog.dto;

import java.util.List;

/**
 * 批量操作结果响应DTO
 */
public class AdminBatchActionResponse {
    private int successCount;
    private int failureCount;
    private List<Long> successIds;
    private List<FailureItem> failures;

    public AdminBatchActionResponse() {
    }

    public AdminBatchActionResponse(int successCount, int failureCount, List<Long> successIds, List<FailureItem> failures) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.successIds = successIds;
        this.failures = failures;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<Long> getSuccessIds() {
        return successIds;
    }

    public void setSuccessIds(List<Long> successIds) {
        this.successIds = successIds;
    }

    public List<FailureItem> getFailures() {
        return failures;
    }

    public void setFailures(List<FailureItem> failures) {
        this.failures = failures;
    }

    /**
     * 失败项详情
     */
    public static class FailureItem {
        private Long id;
        private String reason;

        public FailureItem() {
        }

        public FailureItem(Long id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
