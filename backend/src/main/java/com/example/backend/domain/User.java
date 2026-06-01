package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;
    
    @Column(nullable = false)
    private String username;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Couple couple;
}
