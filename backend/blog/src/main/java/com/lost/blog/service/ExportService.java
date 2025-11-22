package com.lost.blog.service;

import com.lost.blog.model.Post;
import org.springframework.core.io.ByteArrayResource;

public interface ExportService {

    /**
     * 导出文章为Markdown格式（包含元数据）
     * @param post 文章对象
     * @return Markdown内容的字节数组资源
     */
    ByteArrayResource exportAsMarkdown(Post post);

    /**
     * 导出文章为PDF格式
     * @param post 文章对象
     * @return PDF内容的字节数组资源
     */
    ByteArrayResource exportAsPdf(Post post);

    /**
     * 导出文章为HTML格式（包含元数据）
     * @param post 文章对象
     * @return HTML内容的字节数组资源
     */
    ByteArrayResource exportAsHtml(Post post);
}
