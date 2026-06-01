package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.dto.CoupleDto;
import com.example.backend.exception.CoupleNotFoundException;
import com.example.backend.exception.InvalidInviteCodeException;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoupleServiceTest {

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoupleService coupleService;

    private User testUser;
    private Couple testCouple;

    @BeforeEach
    void setUp() {
        testCouple = Couple.builder()
            .id(1L)
            .inviteCode("ABC123")
            .build();

        testUser = User.builder()
            .id(1L)
            .telegramId(123456789L)
            .username("test_user")
            .couple(testCouple)
            .build();
    }

    @Test
    void createCouple_newUser_createsCoupleAndUser() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(coupleRepository.save(any(Couple.class))).thenAnswer(invocation -> {
            Couple couple = invocation.getArgument(0);
            couple.setId(1L);
            return couple;
        });

        CoupleDto result = coupleService.createCouple(123456789L, "test_user");

        assertThat(result).isNotNull();
        assertThat(result.getInviteCode()).isNotNull();
        assertThat(result.getInviteCode()).hasSize(6);
        verify(userRepository, times(2)).save(any(User.class));
        verify(coupleRepository).save(any(Couple.class));
    }

    @Test
    void joinCouple_validCode_joinsSuccessfully() {
        when(coupleRepository.findByInviteCode("ABC123")).thenReturn(Optional.of(testCouple));
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        CoupleDto result = coupleService.joinCouple(123456789L, "new_user", "ABC123");

        assertThat(result).isNotNull();
        assertThat(result.getInviteCode()).isEqualTo("ABC123");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void joinCouple_invalidCode_throwsException() {
        when(coupleRepository.findByInviteCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coupleService.joinCouple(123456789L, "test_user", "INVALID"))
            .isInstanceOf(InvalidInviteCodeException.class)
            .hasMessage("Неверный код приглашения");
    }

    @Test
    void getCouple_userWithCouple_returnsCouple() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(testUser));

        CoupleDto result = coupleService.getCouple(123456789L);

        assertThat(result).isNotNull();
        assertThat(result.getInviteCode()).isEqualTo("ABC123");
    }

    @Test
    void getCouple_userWithoutCouple_throwsException() {
        User userWithoutCouple = User.builder()
            .telegramId(123456789L)
            .username("test_user")
            .build();
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(userWithoutCouple));

        assertThatThrownBy(() -> coupleService.getCouple(123456789L))
            .isInstanceOf(CoupleNotFoundException.class)
            .hasMessage("Пользователь не состоит в паре");
    }

    @Test
    void leaveCouple_lastUser_deletesCouple() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(testUser));

        coupleService.leaveCouple(123456789L);

        verify(userRepository).save(testUser);
        verify(coupleRepository).delete(testCouple);
    }
}
