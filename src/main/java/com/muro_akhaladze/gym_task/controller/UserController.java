package com.muro_akhaladze.gym_task.controller;

import com.muro_akhaladze.gym_task.dto.user.PhotoUploadResponse;
import com.muro_akhaladze.gym_task.security.AccessGuard;
import com.muro_akhaladze.gym_task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User files", description = "Profile photo upload / download")
public class UserController {
    private final UserService userService;
    private final AccessGuard accessGuard;

    // Constructor injection
    public UserController(UserService userService, AccessGuard accessGuard) {
        this.userService = userService;
        this.accessGuard = accessGuard;
    }

    @PostMapping(value = "/{username}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile photo (multipart/form-data, field name: file)")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(@PathVariable String username,
                                                           @RequestParam("file") MultipartFile file,
                                                           Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        return ResponseEntity.ok(userService.updatePhoto(username, file));
    }

    @GetMapping("/{username}/photo")
    @Operation(summary = "Download a profile photo (any logged-in user)")
    public ResponseEntity<Resource> getPhoto(@PathVariable String username) {
        Resource photo = userService.loadPhoto(username);
        MediaType type = MediaTypeFactory.getMediaType(photo).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFilename() + "\"")
                .body(photo);
    }

    @DeleteMapping("/{username}/photo")
    @Operation(summary = "Delete the profile photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable String username, Authentication authentication) {
        accessGuard.requireSelf(authentication, username);
        userService.deletePhoto(username);
        return ResponseEntity.noContent().build();
    }
}
