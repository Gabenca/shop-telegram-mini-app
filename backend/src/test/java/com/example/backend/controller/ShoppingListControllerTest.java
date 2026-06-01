package com.example.backend.controller;

import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

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

    @Test
    void getShoppingListForWeek_shouldReturn200() throws Exception {
        when(shoppingListService.getShoppingListForWeek(LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(new ShoppingListItemDto()));

        mockMvc.perform(get("/api/shopping-list").param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
