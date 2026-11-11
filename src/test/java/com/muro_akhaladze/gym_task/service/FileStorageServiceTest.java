package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.exceptions.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path dir;

    @Test
    void storesImageUnderGeneratedName() throws Exception {
        FileStorageService storage = new FileStorageService(dir.toString());
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
        String name = storage.store(new MockMultipartFile("file", "me.png", "image/png", png));

        assertThat(name).endsWith(".png").doesNotContain("me");
        assertThat(Files.readAllBytes(dir.resolve(name))).isEqualTo(png);
        assertThat(storage.load(name).exists()).isTrue();

        storage.delete(name);
        assertThat(Files.exists(dir.resolve(name))).isFalse();
    }

    @Test
    void rejectsEmptyAndNonImageFiles() {
        FileStorageService storage = new FileStorageService(dir.toString());
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "x.png", "image/png", new byte[0])))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "x.exe", "application/octet-stream", new byte[]{1})))
                .isInstanceOf(ApiException.class);
        // right Content-Type, wrong bytes
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "x.jpg", "image/jpeg", "text".getBytes())))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsPathTraversal() {
        FileStorageService storage = new FileStorageService(dir.toString());
        assertThatThrownBy(() -> storage.load("../../etc/passwd")).isInstanceOf(ApiException.class);
    }
}
