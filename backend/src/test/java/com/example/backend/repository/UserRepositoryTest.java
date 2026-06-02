package com.example.backend.repository;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByTelegramId_existingUser_returnsUser() {
        User user = User.builder()
            .telegramId(123456789L)
            .username("test_user")
            .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByTelegramId(123456789L);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test_user");
    }

    @Test
    void findByTelegramId_nonExisting_returnsEmpty() {
        Optional<User> result = userRepository.findByTelegramId(999999999L);
        assertThat(result).isEmpty();
    }

    @Test
    void countByCoupleId_returnsCorrectCount() {
        Couple couple = Couple.builder()
            .inviteCode("ABC123")
            .build();
        entityManager.persist(couple);

        User user1 = User.builder()
            .telegramId(111L)
            .username("user1")
            .couple(couple)
            .build();
        User user2 = User.builder()
            .telegramId(222L)
            .username("user2")
            .couple(couple)
            .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        long count = userRepository.countByCoupleId(couple.getId());

        assertThat(count).isEqualTo(2);
    }
}
