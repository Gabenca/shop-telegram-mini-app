package com.example.backend.integration;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CoupleIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @Test
    void createCoupleAndJoin() {
        User creator = User.builder()
            .telegramId(111L)
            .username("creator")
            .build();
        userRepository.save(creator);

        Couple couple = Couple.builder()
            .inviteCode("JOIN01")
            .build();
        coupleRepository.save(couple);

        creator.setCouple(couple);
        userRepository.save(creator);

        User joiner = User.builder()
            .telegramId(222L)
            .username("joiner")
            .couple(couple)
            .build();
        userRepository.save(joiner);

        Optional<User> foundJoiner = userRepository.findByTelegramId(222L);
        assertThat(foundJoiner).isPresent();
        assertThat(foundJoiner.get().getCouple().getInviteCode()).isEqualTo("JOIN01");

        long count = userRepository.countByCoupleId(couple.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void leaveCouple_deletesCoupleWhenLastUser() {
        Couple couple = Couple.builder()
            .inviteCode("LEAVE01")
            .build();
        coupleRepository.save(couple);

        User user = User.builder()
            .telegramId(333L)
            .username("lonely")
            .couple(couple)
            .build();
        userRepository.save(user);

        user.setCouple(null);
        userRepository.save(user);

        long count = userRepository.countByCoupleId(couple.getId());
        assertThat(count).isEqualTo(0);

        Optional<Couple> remaining = coupleRepository.findById(couple.getId());
        assertThat(remaining).isEmpty();
    }
}
