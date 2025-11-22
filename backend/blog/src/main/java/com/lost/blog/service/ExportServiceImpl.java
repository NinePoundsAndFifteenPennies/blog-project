package com.lost.blog.service;

import com.lowagie.text.DocumentException;
import com.lost.blog.model.ContentType;
import com.lost.blog.model.Post;
import com.lost.blog.model.Tag;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ByteArrayResource exportAsMarkdown(Post post) {
        StringBuilder markdown = new StringBuilder();
        
        // 添加文章元数据
        markdown.append("---\n");
        markdown.append("标题: ").append(post.getTitle()).append("\n");
        markdown.append("作者: ").append(post.getUser().getUsername()).append("\n");
        markdown.append("创建时间: ").append(post.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        if (post.getPublishedAt() != null) {
            markdown.append("发布时间: ").append(post.getPublishedAt().format(DATE_FORMATTER)).append("\n");
        }
        if (post.getUpdatedAt() != null) {
            markdown.append("更新时间: ").append(post.getUpdatedAt().format(DATE_FORMATTER)).append("\n");
        }
        if (post.getCategory() != null) {
            markdown.append("分类: ").append(post.getCategory().getName()).append("\n");
        }
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            String tags = post.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(", "));
            markdown.append("标签: ").append(tags).append("\n");
        }
        markdown.append("---\n\n");
        
        // 添加文章内容
        markdown.append("# ").append(post.getTitle()).append("\n\n");
        markdown.append(post.getContent());
        
        byte[] bytes = markdown.toString().getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes);
    }

    @Override
    public ByteArrayResource exportAsPdf(Post post) {
        try {
            // 将内容转换为HTML
            String htmlContent = convertToHtml(post);
            
            // 创建PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            byte[] pdfBytes = outputStream.toByteArray();
            return new ByteArrayResource(pdfBytes);
        } catch (DocumentException e) {
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ByteArrayResource exportAsHtml(Post post) {
        String htmlContent = convertToHtml(post);
        byte[] bytes = htmlContent.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes);
    }

    /**
     * 将文章转换为HTML格式
     */
    private String convertToHtml(Post post) {
        StringBuilder html = new StringBuilder();
        
        // HTML文档头部
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>").append(escapeHtml(post.getTitle())).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Arial', 'Microsoft YaHei', sans-serif; line-height: 1.6; max-width: 800px; margin: 40px auto; padding: 20px; color: #333; }\n");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append(".metadata { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; }\n");
        html.append(".metadata p { margin: 5px 0; }\n");
        html.append(".metadata strong { color: #2c3e50; }\n");
        html.append(".content { margin-top: 30px; }\n");
        html.append("code { background-color: #f4f4f4; padding: 2px 6px; border-radius: 3px; }\n");
        html.append("pre { background-color: #f4f4f4; padding: 15px; border-radius: 5px; overflow-x: auto; }\n");
        html.append("blockquote { border-left: 4px solid #ddd; margin: 0; padding-left: 20px; color: #666; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // 文章标题
        html.append("<h1>").append(escapeHtml(post.getTitle())).append("</h1>\n");
        
        // 文章元数据
        html.append("<div class=\"metadata\">\n");
        html.append("<p><strong>作者:</strong> ").append(escapeHtml(post.getUser().getUsername())).append("</p>\n");
        html.append("<p><strong>创建时间:</strong> ").append(post.getCreatedAt().format(DATE_FORMATTER)).append("</p>\n");
        if (post.getPublishedAt() != null) {
            html.append("<p><strong>发布时间:</strong> ").append(post.getPublishedAt().format(DATE_FORMATTER)).append("</p>\n");
        }
        if (post.getUpdatedAt() != null) {
            html.append("<p><strong>更新时间:</strong> ").append(post.getUpdatedAt().format(DATE_FORMATTER)).append("</p>\n");
        }
        if (post.getCategory() != null) {
            html.append("<p><strong>分类:</strong> ").append(escapeHtml(post.getCategory().getName())).append("</p>\n");
        }
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            String tags = post.getTags().stream()
                    .map(tag -> escapeHtml(tag.getName()))
                    .collect(Collectors.joining(", "));
            html.append("<p><strong>标签:</strong> ").append(tags).append("</p>\n");
        }
        html.append("</div>\n");
        
        // 文章内容
        html.append("<div class=\"content\">\n");
        if (post.getContentType() == ContentType.MARKDOWN) {
            // Markdown转HTML
            Parser parser = Parser.builder().build();
            Node document = parser.parse(post.getContent());
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            html.append(renderer.render(document));
        } else {
            // 直接使用HTML内容
            html.append(post.getContent());
        }
        html.append("</div>\n");
        
        // HTML文档尾部
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
