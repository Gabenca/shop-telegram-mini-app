package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.dto.CoupleDto;
import com.example.backend.dto.UserDto;
import com.example.backend.exception.CoupleNotFoundException;
import com.example.backend.exception.InvalidInviteCodeException;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    @Transactional
    public CoupleDto createCouple(Long telegramId, String username) {
        User user = userRepository.findByTelegramId(telegramId)
            .orElseGet(() -> {
                User newUser = User.builder()
                    .telegramId(telegramId)
                    .username(username)
                    .build();
                return userRepository.save(newUser);
            });

        if (user.getCouple() != null) {
            return mapToDto(user.getCouple());
        }

        Couple couple = Couple.builder()
            .inviteCode(generateInviteCode())
            .build();
        couple = coupleRepository.save(couple);

        user.setCouple(couple);
        userRepository.save(user);

        return mapToDto(couple);
    }

    @Transactional
    public CoupleDto joinCouple(Long telegramId, String username, String inviteCode) {
        Couple couple = coupleRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new InvalidInviteCodeException("Неверный код приглашения"));

        User user = userRepository.findByTelegramId(telegramId)
            .orElseGet(() -> {
                User newUser = User.builder()
                    .telegramId(telegramId)
                    .username(username)
                    .build();
                return userRepository.save(newUser);
            });

        if (user.getCouple() != null) {
            throw new IllegalStateException("Пользователь уже состоит в паре");
        }

        user.setCouple(couple);
        userRepository.save(user);

        return mapToDto(couple);
    }

    @Transactional(readOnly = true)
    public CoupleDto getCouple(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
            .orElseThrow(() -> new CoupleNotFoundException("Пользователь не найден"));

        if (user.getCouple() == null) {
            throw new CoupleNotFoundException("Пользователь не состоит в паре");
        }

        return mapToDto(user.getCouple());
    }

    @Transactional
    public void leaveCouple(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
            .orElseThrow(() -> new CoupleNotFoundException("Пользователь не найден"));

        Couple couple = user.getCouple();
        if (couple == null) {
            return;
        }

        user.setCouple(null);
        userRepository.save(user);

        long remainingCount = userRepository.countByCoupleId(couple.getId());
        if (remainingCount == 0) {
            coupleRepository.delete(couple);
        }
    }

    private String generateInviteCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    private CoupleDto mapToDto(Couple couple) {
        List<UserDto> userDtos = couple.getUsers().stream()
            .map(this::mapUserToDto)
            .collect(Collectors.toList());

        return CoupleDto.builder()
            .id(couple.getId())
            .inviteCode(couple.getInviteCode())
            .users(userDtos)
            .build();
    }

    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .telegramId(user.getTelegramId())
            .username(user.getUsername())
            .build();
    }
}
