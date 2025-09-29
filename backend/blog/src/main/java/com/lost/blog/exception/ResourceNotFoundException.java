package com.lost.blog.exception;

/**
 * 通用资源未找到异常（RuntimeException），所有模块可共享。
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
