package com.muro_akhaladze.gym_task.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoUploadResponse {
    private String username;
    private String storedFileName;
    private String originalFileName;
    private String contentType;
    private long size;
    private String downloadUrl;
}
