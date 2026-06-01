package com.example.backend.controller;

import com.example.backend.dto.CoupleDto;
import com.example.backend.dto.JoinCoupleRequest;
import com.example.backend.service.CoupleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;

    @PostMapping("/create")
    public ResponseEntity<CoupleDto> createCouple(HttpServletRequest request) {
        Long telegramId = (Long) request.getAttribute("telegramId");
        String username = (String) request.getAttribute("username");

        CoupleDto couple = coupleService.createCouple(telegramId, username);
        return ResponseEntity.ok(couple);
    }

    @PostMapping("/join")
    public ResponseEntity<CoupleDto> joinCouple(
            @Valid @RequestBody JoinCoupleRequest joinRequest,
            HttpServletRequest request) {
        Long telegramId = (Long) request.getAttribute("telegramId");
        String username = (String) request.getAttribute("username");

        CoupleDto couple = coupleService.joinCouple(telegramId, username, joinRequest.getInviteCode());
        return ResponseEntity.ok(couple);
    }

    @GetMapping
    public ResponseEntity<CoupleDto> getCouple(HttpServletRequest request) {
        Long telegramId = (Long) request.getAttribute("telegramId");

        CoupleDto couple = coupleService.getCouple(telegramId);
        return ResponseEntity.ok(couple);
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveCouple(HttpServletRequest request) {
        Long telegramId = (Long) request.getAttribute("telegramId");

        coupleService.leaveCouple(telegramId);
        return ResponseEntity.noContent().build();
    }
}
