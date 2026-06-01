package com.example.backend.telegram;

import com.example.backend.dto.PhotoUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramBotService {

    @Value("${telegram.bot-token:}")
    private String botToken;

    private final WebClient.Builder webClientBuilder;

    public PhotoUploadResponse uploadPhoto(MultipartFile file) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendPhoto";

        WebClient webClient = webClientBuilder.build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("chat_id", botToken);
        builder.part("photo", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        }).contentType(MediaType.IMAGE_JPEG);

        Map<String, Object> response = webClient.post()
            .uri(url)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (response != null && (Boolean) response.get("ok")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            Map<String, Object> photo = ((java.util.List<Map<String, Object>>) result.get("photo")).get(0);
            String fileId = (String) photo.get("file_id");

            return PhotoUploadResponse.builder()
                .fileId(fileId)
                .build();
        }

        throw new RuntimeException("Failed to upload photo to Telegram");
    }

    public byte[] getPhoto(String fileId) {
        String getFileUrl = "https://api.telegram.org/bot" + botToken + "/getFile";

        WebClient webClient = webClientBuilder.build();

        Map<String, Object> response = webClient.post()
            .uri(getFileUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("file_id", fileId))
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (response != null && (Boolean) response.get("ok")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            String filePath = (String) result.get("file_path");

            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;

            byte[] fileBytes = webClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            return fileBytes;
        }

        throw new RuntimeException("Failed to get photo from Telegram");
    }
}
