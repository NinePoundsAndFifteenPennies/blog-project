package com.lost.blog.service;

import com.lost.blog.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

class FileServiceImplTest {

    @Test
    void shouldDeleteOldAvatarFileWhenAvatarUrlStartsWithUploads(@TempDir Path tempUploadBaseDir) throws Exception {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        FileServiceImpl fileService = new FileServiceImpl(userRepository);

        ReflectionTestUtils.setField(fileService, "uploadBaseDir", tempUploadBaseDir.toString());

        Path avatarFile = tempUploadBaseDir.resolve("1/avatars/old-avatar.png");
        Files.createDirectories(avatarFile.getParent());
        Files.writeString(avatarFile, "old-avatar");

        Assertions.assertTrue(Files.exists(avatarFile));
        fileService.deleteAvatar("/uploads/1/avatars/old-avatar.png");
        Assertions.assertFalse(Files.exists(avatarFile));
    }
}
