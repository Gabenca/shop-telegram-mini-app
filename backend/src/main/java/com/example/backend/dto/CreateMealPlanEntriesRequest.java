package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateMealPlanEntriesRequest {

    @NotEmpty
    @Valid
    private List<CreateMealPlanEntryRequest> entries;
}
