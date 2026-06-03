package com.example.backend.security;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.exception.UserNotInCoupleException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupleScopeAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    private CoupleScopeAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new CoupleScopeAspect();
    }

    @Test
    void requireCouple_userHasCouple_shouldProceed() throws Throwable {
        User user = User.builder().id(1L).couple(Couple.builder().id(1L).build()).build();
        when(joinPoint.getArgs()).thenReturn(new Object[]{user});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.requireCouple(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void requireCouple_userHasNoCouple_shouldThrow() {
        User user = User.builder().id(1L).couple(null).build();
        when(joinPoint.getArgs()).thenReturn(new Object[]{user});

        assertThatThrownBy(() -> aspect.requireCouple(joinPoint))
            .isInstanceOf(UserNotInCoupleException.class)
            .hasMessage("Пользователь не состоит в паре");

        try {
            verify(joinPoint, never()).proceed();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Test
    void requireCouple_noUserArg_shouldProceed() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"x", 42, List.of()});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.requireCouple(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }
}
