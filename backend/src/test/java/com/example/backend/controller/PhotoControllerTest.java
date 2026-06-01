package com.example.backend.controller;

import com.example.backend.telegram.TelegramBotService;
import com.example.backend.dto.PhotoUploadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramBotService telegramBotService;

    @Test
    void uploadPhoto_validFile_returnsFileId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        PhotoUploadResponse response = PhotoUploadResponse.builder()
            .fileId("test_file_id_123")
            .build();

        when(telegramBotService.uploadPhoto(any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/recipes/upload-photo")
                .file(file)
                .requestAttr("telegramId", 123456789L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").value("test_file_id_123"));
    }
}
