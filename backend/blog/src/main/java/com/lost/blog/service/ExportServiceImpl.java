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
        
        // HTML文档头部 (XHTML格式，用于PDF生成)
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\" />\n");
        html.append("<title>").append(escapeHtml(post.getTitle())).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Arial', 'Microsoft YaHei', 'SimSun', sans-serif; line-height: 1.8; max-width: 900px; margin: 40px auto; padding: 40px; color: #2c3e50; background-color: #ffffff; }\n");
        html.append("h1 { color: #2c3e50; font-size: 32px; font-weight: bold; border-bottom: 3px solid #3498db; padding-bottom: 15px; margin-bottom: 25px; }\n");
        html.append("h2 { color: #34495e; font-size: 26px; font-weight: bold; margin-top: 30px; margin-bottom: 15px; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px; }\n");
        html.append("h3 { color: #34495e; font-size: 22px; font-weight: bold; margin-top: 25px; margin-bottom: 12px; }\n");
        html.append("h4, h5, h6 { color: #34495e; font-weight: bold; margin-top: 20px; margin-bottom: 10px; }\n");
        html.append("p { margin: 15px 0; font-size: 16px; }\n");
        html.append(".metadata { background: linear-gradient(to right, #f8f9fa, #ffffff); padding: 20px; border-left: 5px solid #3498db; margin: 30px 0; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append(".metadata p { margin: 8px 0; font-size: 14px; color: #555; }\n");
        html.append(".metadata strong { color: #2c3e50; font-weight: 600; }\n");
        html.append(".content { margin-top: 40px; }\n");
        html.append("ul, ol { margin: 15px 0; padding-left: 30px; }\n");
        html.append("li { margin: 8px 0; line-height: 1.8; }\n");
        html.append("code { background-color: #f5f5f5; color: #e74c3c; padding: 3px 8px; border-radius: 4px; font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; }\n");
        html.append("pre { background-color: #282c34; color: #abb2bf; padding: 20px; border-radius: 6px; overflow-x: auto; margin: 20px 0; line-height: 1.6; }\n");
        html.append("pre code { background-color: transparent; color: inherit; padding: 0; }\n");
        html.append("blockquote { border-left: 5px solid #3498db; margin: 20px 0; padding: 15px 20px; background-color: #f9f9f9; color: #555; font-style: italic; border-radius: 4px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
        html.append("th { background-color: #3498db; color: white; font-weight: bold; }\n");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("a { color: #3498db; text-decoration: none; }\n");
        html.append("a:hover { text-decoration: underline; }\n");
        html.append("img { max-width: 100%; height: auto; margin: 20px 0; border-radius: 4px; }\n");
        html.append("hr { border: none; border-top: 2px solid #e0e0e0; margin: 30px 0; }\n");
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
