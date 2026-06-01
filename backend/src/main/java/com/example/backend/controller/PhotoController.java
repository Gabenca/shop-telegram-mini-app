package com.example.backend.controller;

import com.example.backend.dto.PhotoUploadResponse;
import com.example.backend.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class PhotoController {

    private final TelegramBotService telegramBotService;

    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        PhotoUploadResponse response = telegramBotService.uploadPhoto(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/photo/{fileId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPhoto(@PathVariable String fileId) {
        byte[] photo = telegramBotService.getPhoto(fileId);
        return ResponseEntity.ok(photo);
    }
}
