package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Saves uploaded files physically on the local disk (directory set by {@code app.upload-dir}). */
@Service
@Slf4j
public class FileStorageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory " + this.uploadDir, e);
        }
        log.info("Uploaded files are stored in {}", this.uploadDir);
    }

    /** Validates and saves the file; returns the generated file name that is stored in the DB. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("EMPTY_FILE", "Uploaded file is empty", HttpStatus.BAD_REQUEST);
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new ApiException("UNSUPPORTED_FILE_TYPE",
                    "Only JPEG, PNG, WEBP or GIF images are allowed", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        if (!hasImageSignature(file, file.getContentType())) {
            throw new ApiException("INVALID_FILE_CONTENT",
                    "File content does not match its type (" + file.getContentType() + ")", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        String storedName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException("STORAGE_ERROR", "Could not save file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Stored upload '{}' as {}", StringUtils.cleanPath(String.valueOf(file.getOriginalFilename())), target);
        return storedName;
    }

    /** Checks the first bytes ("magic numbers") so a renamed text/exe file is not accepted as an image. */
    private boolean hasImageSignature(MultipartFile file, String contentType) {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(h, 0, h.length);
        } catch (IOException e) {
            return false;
        }
        return switch (contentType) {
            case "image/jpeg" -> n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
            case "image/png" -> n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                    && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A;
            case "image/gif" -> n >= 4 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8';
            case "image/webp" -> n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            default -> false;
        };
    }

    public Resource load(String storedName) {
        try {
            Resource resource = new UrlResource(resolveSafe(storedName).toUri());
            if (!resource.exists()) {
                throw new ApiException("NOT_FOUND", "File not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ApiException("NOT_FOUND", "File not found", HttpStatus.NOT_FOUND);
        }
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolveSafe(storedName));
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", storedName, e.getMessage());
        }
    }

    private Path resolveSafe(String storedName) {
        Path path = uploadDir.resolve(storedName).normalize();
        if (!path.startsWith(uploadDir)) {
            throw new ApiException("INVALID_PATH", "Invalid file path", HttpStatus.BAD_REQUEST);
        }
        return path;
    }
}
