package com.lost.blog.controller;

import com.lost.blog.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传接口（头像等）")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "上传头像", description = "上传用户头像文件（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "400", description = "文件格式或大小不符合要求")
    })
    public ResponseEntity<?> uploadAvatar(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("file") MultipartFile file) {
        
        // 验证用户身份
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登录或token无效，请先登录");
        }

        try {
            String fileUrl = fileService.uploadAvatar(currentUser.getUsername(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        }
    }

    @PutMapping(value = "/update/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "更新头像", description = "更新用户头像文件（需登录，会删除旧头像）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "400", description = "文件格式或大小不符合要求")
    })
    public ResponseEntity<?> updateAvatar(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("file") MultipartFile file) {
        
        // 验证用户身份
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登录或token无效，请先登录");
        }

        try {
            String fileUrl = fileService.updateAvatar(currentUser.getUsername(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件更新失败: " + e.getMessage());
        }
    }
}
