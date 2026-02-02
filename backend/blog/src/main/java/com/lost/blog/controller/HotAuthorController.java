package com.lost.blog.controller;

import com.lost.blog.dto.HotAuthorResponse;
import com.lost.blog.service.HotAuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 热门作者控制器
 * 提供热门作者查询接口
 */
@RestController
@RequestMapping("/api/authors")
public class HotAuthorController {

    private final HotAuthorService hotAuthorService;

    @Autowired
    public HotAuthorController(HotAuthorService hotAuthorService) {
        this.hotAuthorService = hotAuthorService;
    }

    /**
     * 获取热门作者列表
     * 公开接口，无需认证
     * 
     * @param limit 返回的作者数量（默认30，最大30）
     * @return 热门作者列表
     */
    @GetMapping("/hot")
    public ResponseEntity<List<HotAuthorResponse>> getHotAuthors(
            @RequestParam(defaultValue = "30") int limit) {
        List<HotAuthorResponse> hotAuthors = hotAuthorService.getHotAuthors(limit);
        return ResponseEntity.ok(hotAuthors);
    }
}
