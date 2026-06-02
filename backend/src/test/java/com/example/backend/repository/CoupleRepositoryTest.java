package com.example.backend.repository;

import com.example.backend.domain.Couple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoupleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CoupleRepository coupleRepository;

    @Test
    void findByInviteCode_existingCouple_returnsCouple() {
        Couple couple = Couple.builder()
            .inviteCode("XYZ789")
            .build();
        entityManager.persist(couple);
        entityManager.flush();

        Optional<Couple> result = coupleRepository.findByInviteCode("XYZ789");

        assertThat(result).isPresent();
        assertThat(result.get().getInviteCode()).isEqualTo("XYZ789");
    }

    @Test
    void findByInviteCode_nonExisting_returnsEmpty() {
        Optional<Couple> result = coupleRepository.findByInviteCode("INVALID");
        assertThat(result).isEmpty();
    }
}
