package com.example.backend.controller;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingListController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingListService shoppingListService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getShoppingListForWeek_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).telegramId(123L).username("test").couple(new Couple()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shoppingListService.getShoppingListForWeek(any(), any()))
                .thenReturn(List.of(new ShoppingListItemDto()));

        mockMvc.perform(get("/api/shopping-list")
                .param("weekStart", "2026-06-01")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
