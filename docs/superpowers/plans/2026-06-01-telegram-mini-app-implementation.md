# Telegram Mini App для пар — План реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создать Telegram Mini App для пар с совместным управлением рецептами, планированием питания и списком покупок.

**Architecture:** Spring Boot backend (Java 21) с PostgreSQL + Angular 18 frontend (standalone components). Backend расширяет существующую модель данных для поддержки пар через invite-коды. Frontend строится с нуля, используя Telegram Web App SDK для нативной интеграции.

**Tech Stack:** Java 21, Spring Boot 3.3.5, PostgreSQL 15, Angular 18.2, TypeScript 5.5, @twa-dev/sdk, RxJS

---

## Обзор фаз

- **Фаза 1:** Backend — Модель данных для пар (10 задач)
- **Фаза 2:** Backend — Загрузка фото (3 задачи)
- **Фаза 3:** Frontend — Базовая структура (8 задач)
- **Фаза 4:** Frontend — Рецепты (6 задач)
- **Фаза 5:** Frontend — План на неделю (4 задачи)
- **Фаза 6:** Frontend — Список покупок (4 задачи)
- **Фаза 7:** Frontend — Главная и профиль (4 задачи)
- **Фаза 8:** Тестирование и полировка (5 задач)

---

## Фаза 1: Backend — Модель данных для пар

### Задача 1.1: Создать сущности User и Couple

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/domain/User.java`
- Создать: `backend/src/main/java/com/example/backend/domain/Couple.java`
- Создать: `backend/src/main/java/com/example/backend/dto/UserDto.java`
- Создать: `backend/src/main/java/com/example/backend/dto/CoupleDto.java`

- [ ] **Шаг 1: Создать сущность User**

```java
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
```

- [ ] **Шаг 2: Создать сущность Couple**

```java
package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "couples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Couple {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invite_code", unique = true, nullable = false, length = 6)
    private String inviteCode;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<User> users = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Шаг 3: Создать DTO для User**

```java
package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private Long telegramId;
    private String username;
}
```

- [ ] **Шаг 4: Создать DTO для Couple**

```java
package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleDto {
    private Long id;
    private String inviteCode;
    private List<UserDto> users;
}
```

- [ ] **Шаг 5: Коммит**

```bash
git add backend/src/main/java/com/example/backend/domain/User.java \
        backend/src/main/java/com/example/backend/domain/Couple.java \
        backend/src/main/java/com/example/backend/dto/UserDto.java \
        backend/src/main/java/com/example/backend/dto/CoupleDto.java
git commit -m "feat: add User and Couple domain entities with DTOs"
```

---

### Задача 1.2: Обновить существующие сущности и enum

**Файлы:**
- Изменить: `backend/src/main/java/com/example/backend/domain/Recipe.java`
- Изменить: `backend/src/main/java/com/example/backend/domain/MealPlanEntry.java`
- Изменить: `backend/src/main/java/com/example/backend/domain/ShoppingListItem.java`
- Изменить: `backend/src/main/java/com/example/backend/domain/MealType.java`

- [ ] **Шаг 1: Обновить enum MealType**

Заменить содержимое `backend/src/main/java/com/example/backend/domain/MealType.java`:

```java
package com.example.backend.domain;

public enum MealType {
    BREAKFAST,
    LUNCH,
    AFTERNOON_SNACK,
    DINNER
}
```

- [ ] **Шаг 2: Добавить couple_id в Recipe**

В файл `backend/src/main/java/com/example/backend/domain/Recipe.java` добавить поле:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "couple_id", nullable = false)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Couple couple;
```

- [ ] **Шаг 3: Добавить couple_id в MealPlanEntry**

В файл `backend/src/main/java/com/example/backend/domain/MealPlanEntry.java` добавить поле:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "couple_id", nullable = false)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Couple couple;
```

- [ ] **Шаг 4: Добавить couple_id в ShoppingListItem**

В файл `backend/src/main/java/com/example/backend/domain/ShoppingListItem.java` добавить поле:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "couple_id", nullable = false)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Couple couple;
```

- [ ] **Шаг 5: Коммит**

```bash
git add backend/src/main/java/com/example/backend/domain/
git commit -m "feat: add couple_id to Recipe, MealPlanEntry, ShoppingListItem and update MealType enum"
```

---

### Задача 1.3: Создать репозитории для User и Couple

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/repository/UserRepository.java`
- Создать: `backend/src/main/java/com/example/backend/repository/CoupleRepository.java`
- Изменить: `backend/src/main/java/com/example/backend/repository/RecipeRepository.java`
- Изменить: `backend/src/main/java/com/example/backend/repository/MealPlanEntryRepository.java`
- Изменить: `backend/src/main/java/com/example/backend/repository/ShoppingListItemRepository.java`

- [ ] **Шаг 1: Создать UserRepository**

```java
package com.example.backend.repository;

import com.example.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
}
```

- [ ] **Шаг 2: Создать CoupleRepository**

```java
package com.example.backend.repository;

import com.example.backend.domain.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByInviteCode(String inviteCode);
}
```

- [ ] **Шаг 3: Обновить RecipeRepository**

В файл `backend/src/main/java/com/example/backend/repository/RecipeRepository.java` добавить метод:

```java
List<Recipe> findByCoupleId(Long coupleId);
```

- [ ] **Шаг 4: Обновить MealPlanEntryRepository**

В файл `backend/src/main/java/com/example/backend/repository/MealPlanEntryRepository.java` добавить метод:

```java
List<MealPlanEntry> findByDateBetweenAndCoupleId(LocalDate start, LocalDate end, Long coupleId);
```

- [ ] **Шаг 5: Обновить ShoppingListItemRepository**

В файл `backend/src/main/java/com/example/backend/repository/ShoppingListItemRepository.java` добавить метод:

```java
List<ShoppingListItem> findByWeekStartDateAndCoupleId(LocalDate weekStartDate, Long coupleId);
```

- [ ] **Шаг 6: Коммит**

```bash
git add backend/src/main/java/com/example/backend/repository/
git commit -m "feat: add User and Couple repositories, update existing repositories with couple_id filtering"
```

---

### Задача 1.4: Создать CoupleService

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/service/CoupleService.java`
- Создать: `backend/src/main/java/com/example/backend/exception/CoupleNotFoundException.java`
- Создать: `backend/src/main/java/com/example/backend/exception/InvalidInviteCodeException.java`

- [ ] **Шаг 1: Создать классы исключений**

Создать `backend/src/main/java/com/example/backend/exception/CoupleNotFoundException.java`:

```java
package com.example.backend.exception;

public class CoupleNotFoundException extends RuntimeException {
    public CoupleNotFoundException(String message) {
        super(message);
    }
}
```

Создать `backend/src/main/java/com/example/backend/exception/InvalidInviteCodeException.java`:

```java
package com.example.backend.exception;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException(String message) {
        super(message);
    }
}
```

- [ ] **Шаг 2: Создать CoupleService**

```java
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
        
        List<User> remainingUsers = couple.getUsers().stream()
            .filter(u -> u.getCouple() != null)
            .collect(Collectors.toList());
        
        if (remainingUsers.isEmpty()) {
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
```

- [ ] **Шаг 3: Коммит**

```bash
git add backend/src/main/java/com/example/backend/service/CoupleService.java \
        backend/src/main/java/com/example/backend/exception/
git commit -m "feat: implement CoupleService with create, join, get, and leave functionality"
```

---

### Задача 1.5: Создать CoupleController

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/controller/CoupleController.java`
- Создать: `backend/src/main/java/com/example/backend/dto/JoinCoupleRequest.java`

- [ ] **Шаг 1: Создать JoinCoupleRequest DTO**

```java
package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinCoupleRequest {
    
    @NotBlank(message = "Код приглашения обязателен")
    @Size(min = 6, max = 6, message = "Код должен содержать 6 символов")
    private String inviteCode;
}
```

- [ ] **Шаг 2: Создать CoupleController**

```java
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
```

- [ ] **Шаг 3: Коммит**

```bash
git add backend/src/main/java/com/example/backend/controller/CoupleController.java \
        backend/src/main/java/com/example/backend/dto/JoinCoupleRequest.java
git commit -m "feat: add CoupleController with create, join, get, and leave endpoints"
```

---

### Задача 1.6: Обновить TelegramInitDataFilter

**Файлы:**
- Изменить: `backend/src/main/java/com/example/backend/telegram/TelegramInitDataFilter.java`

- [ ] **Шаг 1: Обновить TelegramInitDataFilter**

Заменить содержимое `backend/src/main/java/com/example/backend/telegram/TelegramInitDataFilter.java`:

```java
package com.example.backend.telegram;

import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
public class TelegramInitDataFilter implements Filter {
    
    @Value("${telegram.bot-token:}")
    private String botToken;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        if ("dev".equals(activeProfile)) {
            Long devTelegramId = 123456789L;
            String devUsername = "dev_user";
            
            User user = userRepository.findByTelegramId(devTelegramId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                        .telegramId(devTelegramId)
                        .username(devUsername)
                        .build();
                    return userRepository.save(newUser);
                });
            
            request.setAttribute("telegramId", devTelegramId);
            request.setAttribute("username", devUsername);
            request.setAttribute("userId", user.getId());
            
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        String initData = request.getHeader("X-Telegram-Init-Data");
        if (initData == null) {
            initData = request.getParameter("initData");
        }
        
        if (initData == null || initData.isEmpty()) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        try {
            if (!validateInitData(initData)) {
                chain.doFilter(servletRequest, servletResponse);
                return;
            }
            
            Map<String, String> params = parseInitData(initData);
            String userJson = params.get("user");
            
            if (userJson != null) {
                Map<String, Object> userData = parseJson(userJson);
                Long telegramId = Long.valueOf(userData.get("id").toString());
                String username = userData.getOrDefault("username", "").toString();
                
                User user = userRepository.findByTelegramId(telegramId)
                    .orElseGet(() -> {
                        User newUser = User.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .build();
                        return userRepository.save(newUser);
                    });
                
                request.setAttribute("telegramId", telegramId);
                request.setAttribute("username", username);
                request.setAttribute("userId", user.getId());
            }
        } catch (Exception e) {
            // Log error and continue
        }
        
        chain.doFilter(servletRequest, servletResponse);
    }
    
    private boolean validateInitData(String initData) throws NoSuchAlgorithmException, InvalidKeyException {
        Map<String, String> params = parseInitData(initData);
        String hash = params.get("hash");
        
        if (hash == null) {
            return false;
        }
        
        StringBuilder dataCheckString = new StringBuilder();
        params.entrySet().stream()
            .filter(e -> !e.getKey().equals("hash"))
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                if (dataCheckString.length() > 0) {
                    dataCheckString.append("\n");
                }
                dataCheckString.append(e.getKey()).append("=").append(e.getValue());
            });
        
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] secretKeyBytes = hmacSha256.doFinal(botToken.getBytes(StandardCharsets.UTF_8));
        
        SecretKeySpec finalKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        hmacSha256.init(finalKey);
        byte[] computedHash = hmacSha256.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));
        
        String computedHashHex = bytesToHex(computedHash);
        return computedHashHex.equals(hash);
    }
    
    private Map<String, String> parseInitData(String initData) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }
    
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                int idx = pair.indexOf(":");
                if (idx > 0) {
                    String key = pair.substring(0, idx).trim();
                    key = key.replaceAll("^\"|\"$", "");
                    String value = pair.substring(idx + 1).trim();
                    value = value.replaceAll("^\"|\"$", "");
                    result.put(key, value);
                }
            }
        }
        return result;
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
```

- [ ] **Шаг 2: Коммит**

```bash
git add backend/src/main/java/com/example/backend/telegram/TelegramInitDataFilter.java
git commit -m "feat: update TelegramInitDataFilter to extract user info and create User entity"
```

---

### Задача 1.7: Обновить существующие сервисы для фильтрации по couple_id

**Файлы:**
- Изменить: `backend/src/main/java/com/example/backend/service/RecipeService.java`
- Изменить: `backend/src/main/java/com/example/backend/service/MealPlanService.java`
- Изменить: `backend/src/main/java/com/example/backend/service/ShoppingListService.java`

- [ ] **Шаг 1: Обновить RecipeService**

В `RecipeService.java` изменить метод `getAllRecipes`:

```java
@Transactional(readOnly = true)
public List<RecipeDto> getAllRecipes(Long coupleId) {
    return recipeRepository.findByCoupleId(coupleId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}
```

Изменить метод `createRecipe`, добавив установку couple:

```java
@Transactional
public RecipeDto createRecipe(CreateRecipeRequest request, Long coupleId) {
    Couple couple = coupleRepository.findById(coupleId)
        .orElseThrow(() -> new RuntimeException("Couple not found"));
    
    Recipe recipe = Recipe.builder()
        .name(request.getName())
        .description(request.getDescription())
        .photoUrl(request.getPhotoUrl())
        .instructions(request.getInstructions())
        .couple(couple)
        .build();
    
    // ... остальной код без изменений
}
```

Добавить зависимость:

```java
private final CoupleRepository coupleRepository;
```

- [ ] **Шаг 2: Обновить MealPlanService**

Изменить метод `getMealPlanForWeek`:

```java
@Transactional(readOnly = true)
public List<MealPlanEntryDto> getMealPlanForWeek(LocalDate weekStart, Long coupleId) {
    LocalDate weekEnd = weekStart.plusDays(6);
    return mealPlanEntryRepository.findByDateBetweenAndCoupleId(weekStart, weekEnd, coupleId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}
```

Изменить метод `addMealPlanEntry`, добавив установку couple:

```java
@Transactional
public MealPlanEntryDto addMealPlanEntry(CreateMealPlanEntryRequest request, Long coupleId) {
    Recipe recipe = recipeRepository.findById(request.getRecipeId())
        .orElseThrow(() -> new RuntimeException("Recipe not found"));
    
    Couple couple = coupleRepository.findById(coupleId)
        .orElseThrow(() -> new RuntimeException("Couple not found"));
    
    MealPlanEntry entry = MealPlanEntry.builder()
        .date(request.getDate())
        .recipe(recipe)
        .mealType(request.getMealType())
        .couple(couple)
        .build();
    
    entry = mealPlanEntryRepository.save(entry);
    return mapToDto(entry);
}
```

Добавить зависимость:

```java
private final CoupleRepository coupleRepository;
```

- [ ] **Шаг 3: Обновить ShoppingListService**

Изменить метод `getShoppingListForWeek`:

```java
@Transactional(readOnly = true)
public List<ShoppingListItemDto> getShoppingListForWeek(LocalDate weekStart, Long coupleId) {
    return shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, coupleId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}
```

Изменить метод `regenerateShoppingList`, добавив фильтрацию по couple:

```java
@Transactional
public List<ShoppingListItemDto> regenerateShoppingList(LocalDate weekStart, Long coupleId) {
    LocalDate weekEnd = weekStart.plusDays(6);
    List<MealPlanEntry> mealPlan = mealPlanEntryRepository.findByDateBetweenAndCoupleId(weekStart, weekEnd, coupleId);
    
    // Удалить только автоматические товары
    List<ShoppingListItem> existingItems = shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, coupleId);
    shoppingListItemRepository.deleteAll(existingItems.stream()
        .filter(item -> !item.isManual())
        .collect(Collectors.toList()));
    
    // ... остальной код агрегации ингредиентов
    
    // Сохранить новые товары с couple
    Couple couple = coupleRepository.findById(coupleId)
        .orElseThrow(() -> new RuntimeException("Couple not found"));
    
    for (ShoppingListItem item : aggregatedItems) {
        item.setCouple(couple);
        shoppingListItemRepository.save(item);
    }
    
    return shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, coupleId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}
```

Изменить метод `addManualItem`, добавив установку couple:

```java
@Transactional
public ShoppingListItemDto addManualItem(CreateManualItemRequest request, LocalDate weekStart, Long coupleId) {
    Couple couple = coupleRepository.findById(coupleId)
        .orElseThrow(() -> new RuntimeException("Couple not found"));
    
    ShoppingListItem item = ShoppingListItem.builder()
        .weekStartDate(weekStart)
        .ingredientName(request.getIngredientName())
        .totalQuantity(request.getTotalQuantity())
        .unit(request.getUnit())
        .checked(false)
        .manual(true)
        .couple(couple)
        .build();
    
    item = shoppingListItemRepository.save(item);
    return mapToDto(item);
}
```

Добавить зависимость:

```java
private final CoupleRepository coupleRepository;
```

- [ ] **Шаг 4: Коммит**

```bash
git add backend/src/main/java/com/example/backend/service/
git commit -m "feat: update RecipeService, MealPlanService, ShoppingListService to filter by couple_id"
```

---

### Задача 1.8: Обновить контроллеры для передачи couple_id

**Файлы:**
- Изменить: `backend/src/main/java/com/example/backend/controller/RecipeController.java`
- Изменить: `backend/src/main/java/com/example/backend/controller/MealPlanController.java`
- Изменить: `backend/src/main/java/com/example/backend/controller/ShoppingListController.java`

- [ ] **Шаг 1: Обновить RecipeController**

Изменить метод `getAllRecipes`:

```java
@GetMapping
public ResponseEntity<List<RecipeDto>> getAllRecipes(HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    List<RecipeDto> recipes = recipeService.getAllRecipes(user.getCouple().getId());
    return ResponseEntity.ok(recipes);
}
```

Изменить метод `createRecipe`:

```java
@PostMapping
public ResponseEntity<RecipeDto> createRecipe(
        @Valid @RequestBody CreateRecipeRequest createRequest,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.badRequest().build();
    }
    
    RecipeDto recipe = recipeService.createRecipe(createRequest, user.getCouple().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
}
```

Добавить вспомогательный метод:

```java
private User getUserFromRequest(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
}
```

Добавить зависимость:

```java
private final UserRepository userRepository;
```

- [ ] **Шаг 2: Обновить MealPlanController**

Изменить метод `getMealPlanForWeek`:

```java
@GetMapping
public ResponseEntity<List<MealPlanEntryDto>> getMealPlanForWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    List<MealPlanEntryDto> mealPlan = mealPlanService.getMealPlanForWeek(weekStart, user.getCouple().getId());
    return ResponseEntity.ok(mealPlan);
}
```

Изменить метод `addMealPlanEntry`:

```java
@PostMapping
public ResponseEntity<MealPlanEntryDto> addMealPlanEntry(
        @Valid @RequestBody CreateMealPlanEntryRequest createRequest,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.badRequest().build();
    }
    
    MealPlanEntryDto entry = mealPlanService.addMealPlanEntry(createRequest, user.getCouple().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(entry);
}
```

Добавить вспомогательный метод и зависимость (как в RecipeController).

- [ ] **Шаг 3: Обновить ShoppingListController**

Изменить метод `getShoppingListForWeek`:

```java
@GetMapping
public ResponseEntity<List<ShoppingListItemDto>> getShoppingListForWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    List<ShoppingListItemDto> shoppingList = shoppingListService.getShoppingListForWeek(weekStart, user.getCouple().getId());
    return ResponseEntity.ok(shoppingList);
}
```

Изменить метод `regenerateShoppingList`:

```java
@PostMapping("/regenerate")
public ResponseEntity<List<ShoppingListItemDto>> regenerateShoppingList(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.badRequest().build();
    }
    
    List<ShoppingListItemDto> shoppingList = shoppingListService.regenerateShoppingList(weekStart, user.getCouple().getId());
    return ResponseEntity.ok(shoppingList);
}
```

Изменить метод `addManualItem`:

```java
@PostMapping("/items")
public ResponseEntity<ShoppingListItemDto> addManualItem(
        @Valid @RequestBody CreateManualItemRequest createRequest,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
        HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user.getCouple() == null) {
        return ResponseEntity.badRequest().build();
    }
    
    ShoppingListItemDto item = shoppingListService.addManualItem(createRequest, weekStart, user.getCouple().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(item);
}
```

Добавить вспомогательный метод и зависимость (как в RecipeController).

- [ ] **Шаг 4: Коммит**

```bash
git add backend/src/main/java/com/example/backend/controller/
git commit -m "feat: update controllers to extract couple_id from request and pass to services"
```

---

### Задача 1.9: Создать GlobalExceptionHandler

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/exception/GlobalExceptionHandler.java`
- Создать: `backend/src/main/java/com/example/backend/exception/ErrorResponse.java`

- [ ] **Шаг 1: Создать ErrorResponse**

```java
package com.example.backend.exception;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
```

- [ ] **Шаг 2: Создать GlobalExceptionHandler**

```java
package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCoupleNotFound(CoupleNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidInviteCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInviteCode(InvalidInviteCodeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Ошибка валидации");
        
        ErrorResponse error = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

- [ ] **Шаг 3: Коммит**

```bash
git add backend/src/main/java/com/example/backend/exception/
git commit -m "feat: add GlobalExceptionHandler for centralized error handling"
```

---

### Задача 1.10: Написать тесты для CoupleService

**Файлы:**
- Создать: `backend/src/test/java/com/example/backend/service/CoupleServiceTest.java`

- [ ] **Шаг 1: Создать CoupleServiceTest**

```java
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
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User newUser = User.builder()
            .telegramId(123456789L)
            .username("new_user")
            .build();
        
        CoupleDto result = coupleService.joinCouple(123456789L, "new_user", "ABC123");
        
        assertThat(result).isNotNull();
        assertThat(result.getInviteCode()).isEqualTo("ABC123");
        verify(userRepository).save(any(User.class));
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
```

- [ ] **Шаг 2: Запустить тесты**

```bash
cd backend
./gradlew test --tests CoupleServiceTest
```

Ожидаемый результат: все 6 тестов проходят.

- [ ] **Шаг 3: Коммит**

```bash
git add backend/src/test/java/com/example/backend/service/CoupleServiceTest.java
git commit -m "test: add comprehensive tests for CoupleService"
```

---

## Фаза 2: Backend — Загрузка фото

### Задача 2.1: Создать TelegramBotService

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/telegram/TelegramBotService.java`
- Создать: `backend/src/main/java/com/example/backend/dto/PhotoUploadResponse.java`

- [ ] **Шаг 1: Добавить зависимость для HTTP клиента**

В `backend/build.gradle.kts` добавить:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-webflux")
```

- [ ] **Шаг 2: Создать PhotoUploadResponse DTO**

```java
package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadResponse {
    private String fileId;
}
```

- [ ] **Шаг 3: Создать TelegramBotService**

```java
package com.example.backend.telegram;

import com.example.backend.dto.PhotoUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramBotService {
    
    @Value("${telegram.bot-token:}")
    private String botToken;
    
    private final WebClient.Builder webClientBuilder;
    
    public PhotoUploadResponse uploadPhoto(MultipartFile file) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
        
        WebClient webClient = webClientBuilder.build();
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("chat_id", botToken);
        builder.part("photo", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        }).contentType(MediaType.IMAGE_JPEG);
        
        Map<String, Object> response = webClient.post()
            .uri(url)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(Map.class)
            .block();
        
        if (response != null && (Boolean) response.get("ok")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            Map<String, Object> photo = ((java.util.List<Map<String, Object>>) result.get("photo")).get(0);
            String fileId = (String) photo.get("file_id");
            
            return PhotoUploadResponse.builder()
                .fileId(fileId)
                .build();
        }
        
        throw new RuntimeException("Failed to upload photo to Telegram");
    }
    
    public byte[] getPhoto(String fileId) {
        String getFileUrl = "https://api.telegram.org/bot" + botToken + "/getFile";
        
        WebClient webClient = webClientBuilder.build();
        
        Map<String, Object> response = webClient.post()
            .uri(getFileUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("file_id", fileId))
            .retrieve()
            .bodyToMono(Map.class)
            .block();
        
        if (response != null && (Boolean) response.get("ok")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            String filePath = (String) result.get("file_path");
            
            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
            
            byte[] fileBytes = webClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
            
            return fileBytes;
        }
        
        throw new RuntimeException("Failed to get photo from Telegram");
    }
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add backend/src/main/java/com/example/backend/telegram/TelegramBotService.java \
        backend/src/main/java/com/example/backend/dto/PhotoUploadResponse.java \
        backend/build.gradle.kts
git commit -m "feat: add TelegramBotService for photo upload and retrieval via Telegram Bot API"
```

---

### Задача 2.2: Создать PhotoController

**Файлы:**
- Создать: `backend/src/main/java/com/example/backend/controller/PhotoController.java`

- [ ] **Шаг 1: Создать PhotoController**

```java
package com.example.backend.controller;

import com.example.backend.dto.PhotoUploadResponse;
import com.example.backend.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class PhotoController {
    
    private final TelegramBotService telegramBotService;
    
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        PhotoUploadResponse response = telegramBotService.uploadPhoto(file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping(value = "/photo/{fileId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPhoto(@PathVariable String fileId) {
        byte[] photo = telegramBotService.getPhoto(fileId);
        return ResponseEntity.ok(photo);
    }
}
```

- [ ] **Шаг 2: Коммит**

```bash
git add backend/src/main/java/com/example/backend/controller/PhotoController.java
git commit -m "feat: add PhotoController with upload and download endpoints"
```

---

### Задача 2.3: Написать тесты для PhotoController

**Файлы:**
- Создать: `backend/src/test/java/com/example/backend/controller/PhotoControllerTest.java`

- [ ] **Шаг 1: Создать PhotoControllerTest**

```java
package com.example.backend.controller;

import com.example.backend.telegram.TelegramBotService;
import com.example.backend.dto.PhotoUploadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PhotoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TelegramBotService telegramBotService;
    
    @Test
    void uploadPhoto_validFile_returnsFileId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
        
        PhotoUploadResponse response = PhotoUploadResponse.builder()
            .fileId("test_file_id_123")
            .build();
        
        when(telegramBotService.uploadPhoto(any())).thenReturn(response);
        
        mockMvc.perform(multipart("/api/recipes/upload-photo")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").value("test_file_id_123"));
    }
}
```

- [ ] **Шаг 2: Запустить тесты**

```bash
cd backend
./gradlew test --tests PhotoControllerTest
```

Ожидаемый результат: тест проходит.

- [ ] **Шаг 3: Коммит**

```bash
git add backend/src/test/java/com/example/backend/controller/PhotoControllerTest.java
git commit -m "test: add PhotoControllerTest for upload endpoint"
```

---

## Фаза 3: Frontend — Базовая структура

### Задача 3.1: Установить зависимости и настроить Angular

**Файлы:**
- Изменить: `frontend/package.json`
- Изменить: `frontend/src/app/app.config.ts`

- [ ] **Шаг 1: Установить @twa-dev/sdk**

```bash
cd frontend
npm install @twa-dev/sdk
```

- [ ] **Шаг 2: Обновить app.config.ts**

Заменить содержимое `frontend/src/app/app.config.ts`:

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { telegramInitDataInterceptor } from './core/interceptors/telegram-init-data.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([telegramInitDataInterceptor]))
  ]
};
```

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/package.json frontend/package-lock.json frontend/src/app/app.config.ts
git commit -m "feat: install @twa-dev/sdk and configure HttpClient with interceptor"
```

---

### Задача 3.2: Создать TypeScript интерфейсы

**Файлы:**
- Создать: `frontend/src/app/shared/models/index.ts`

- [ ] **Шаг 1: Создать модели**

```typescript
export interface User {
  id: number;
  telegramId: number;
  username: string;
}

export interface Couple {
  id: number;
  inviteCode: string;
  users: User[];
}

export interface Recipe {
  id: number;
  name: string;
  description: string;
  photoUrl: string;
  instructions: string;
  ingredients: Ingredient[];
  createdAt: string;
  updatedAt: string;
}

export interface Ingredient {
  id: number;
  name: string;
  weightInGrams: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface MealPlanEntry {
  id: number;
  date: string;
  recipe: Recipe;
  mealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER';
}

export interface ShoppingListItem {
  id: number;
  weekStartDate: string;
  ingredientName: string;
  totalQuantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
  checked: boolean;
  manual: boolean;
}

export interface CreateRecipeRequest {
  name: string;
  description: string;
  photoUrl: string;
  instructions: string;
  ingredients: IngredientRequest[];
}

export interface IngredientRequest {
  name: string;
  weightInGrams: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface CreateMealPlanEntryRequest {
  date: string;
  recipeId: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER';
}

export interface CreateManualItemRequest {
  ingredientName: string;
  totalQuantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

export interface JoinCoupleRequest {
  inviteCode: string;
}
```

- [ ] **Шаг 2: Коммит**

```bash
git add frontend/src/app/shared/models/index.ts
git commit -m "feat: add TypeScript interfaces for all domain models and DTOs"
```

---

### Задача 3.3: Создать TelegramInitDataInterceptor

**Файлы:**
- Создать: `frontend/src/app/core/interceptors/telegram-init-data.interceptor.ts`

- [ ] **Шаг 1: Создать interceptor**

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import WebApp from '@twa-dev/sdk';

export const telegramInitDataInterceptor: HttpInterceptorFn = (req, next) => {
  const initData = WebApp.initData;
  
  if (initData) {
    const clonedReq = req.clone({
      setHeaders: {
        'X-Telegram-Init-Data': initData
      }
    });
    return next(clonedReq);
  }
  
  return next(req);
};
```

- [ ] **Шаг 2: Коммит**

```bash
git add frontend/src/app/core/interceptors/telegram-init-data.interceptor.ts
git commit -m "feat: add TelegramInitDataInterceptor to attach init data to HTTP requests"
```

---

### Задача 3.4: Создать TelegramService

**Файлы:**
- Создать: `frontend/src/app/core/services/telegram.service.ts`

- [ ] **Шаг 1: Создать TelegramService**

```typescript
import { Injectable } from '@angular/core';
import WebApp from '@twa-dev/sdk';

@Injectable({
  providedIn: 'root'
})
export class TelegramService {
  
  private webApp = WebApp;
  
  constructor() {
    this.webApp.ready();
    this.webApp.expand();
  }
  
  getMainButton() {
    return this.webApp.MainButton;
  }
  
  getBackButton() {
    return this.webApp.BackButton;
  }
  
  showMainButton(text: string, onClick: () => void) {
    const mainButton = this.getMainButton();
    mainButton.setText(text);
    mainButton.onClick(onClick);
    mainButton.show();
  }
  
  hideMainButton() {
    this.getMainButton().hide();
  }
  
  showBackButton(onClick: () => void) {
    const backButton = this.getBackButton();
    backButton.onClick(onClick);
    backButton.show();
  }
  
  hideBackButton() {
    this.getBackButton().hide();
  }
  
  openCamera(callback: (photo: string) => void) {
    this.webApp.openCamera({
      capture: 'camera'
    }, (photo) => {
      if (photo) {
        callback(photo);
      }
    });
  }
  
  openGallery(callback: (photo: string) => void) {
    this.webApp.openGallery({
      multiple: false
    }, (photo) => {
      if (photo) {
        callback(photo);
      }
    });
  }
  
  share(url: string, text?: string) {
    this.webApp.openTelegramLink(`https://t.me/share/url?url=${encodeURIComponent(url)}${text ? '&text=' + encodeURIComponent(text) : ''}`);
  }
  
  hapticFeedback(type: 'light' | 'medium' | 'heavy' | 'rigid' | 'soft' = 'light') {
    this.webApp.HapticFeedback.impactOccurred(type);
  }
  
  showPopup(title: string, message: string, buttons: string[] = ['OK']) {
    this.webApp.showPopup({
      title,
      message,
      buttons
    });
  }
  
  close() {
    this.webApp.close();
  }
}
```

- [ ] **Шаг 2: Коммит**

```bash
git add frontend/src/app/core/services/telegram.service.ts
git commit -m "feat: add TelegramService wrapper for Web App SDK"
```

---

### Задача 3.5: Создать CoupleGuard

**Файлы:**
- Создать: `frontend/src/app/core/guards/couple.guard.ts`

- [ ] **Шаг 1: Создать CoupleGuard**

```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { CoupleService } from '../../shared/services/couple.service';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const coupleGuard: CanActivateFn = (route, state) => {
  const coupleService = inject(CoupleService);
  const router = inject(Router);
  
  return coupleService.getCouple().pipe(
    map(couple => {
      if (couple) {
        return true;
      }
      router.navigate(['/join']);
      return false;
    }),
    catchError(() => {
      router.navigate(['/join']);
      return of(false);
    })
  );
};
```

- [ ] **Шаг 2: Коммит**

```bash
git add frontend/src/app/core/guards/couple.guard.ts
git commit -m "feat: add CoupleGuard to redirect to /join if user has no couple"
```

---

### Задача 3.6: Создать API сервисы

**Файлы:**
- Создать: `frontend/src/app/shared/services/api.service.ts`
- Создать: `frontend/src/app/shared/services/couple.service.ts`
- Создать: `frontend/src/app/shared/services/recipe.service.ts`
- Создать: `frontend/src/app/shared/services/meal-plan.service.ts`
- Создать: `frontend/src/app/shared/services/shopping-list.service.ts`

- [ ] **Шаг 1: Создать ApiService (базовый)**

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  protected baseUrl = '/api';
  
  constructor(protected http: HttpClient) {}
}
```

- [ ] **Шаг 2: Создать CoupleService**

```typescript
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Couple, JoinCoupleRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CoupleService extends ApiService {
  
  private couple$ = new BehaviorSubject<Couple | null>(null);
  
  getCoupleObservable(): Observable<Couple | null> {
    return this.couple$.asObservable();
  }
  
  getCouple(): Observable<Couple> {
    return this.http.get<Couple>(`${this.baseUrl}/couple`).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }
  
  createCouple(): Observable<Couple> {
    return this.http.post<Couple>(`${this.baseUrl}/couple/create`, {}).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }
  
  joinCouple(request: JoinCoupleRequest): Observable<Couple> {
    return this.http.post<Couple>(`${this.baseUrl}/couple/join`, request).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }
  
  leaveCouple(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/couple/leave`).pipe(
      tap(() => this.couple$.next(null))
    );
  }
}
```

- [ ] **Шаг 3: Создать RecipeService**

```typescript
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Recipe, CreateRecipeRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class RecipeService extends ApiService {
  
  private recipes$ = new BehaviorSubject<Recipe[]>([]);
  
  getRecipesObservable(): Observable<Recipe[]> {
    return this.recipes$.asObservable();
  }
  
  loadRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.baseUrl}/recipes`).pipe(
      tap(recipes => this.recipes$.next(recipes))
    );
  }
  
  getRecipe(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.baseUrl}/recipes/${id}`);
  }
  
  createRecipe(request: CreateRecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.baseUrl}/recipes`, request).pipe(
      tap(recipe => {
        const current = this.recipes$.value;
        this.recipes$.next([...current, recipe]);
      })
    );
  }
  
  updateRecipe(id: number, request: CreateRecipeRequest): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.baseUrl}/recipes/${id}`, request).pipe(
      tap(updatedRecipe => {
        const current = this.recipes$.value;
        const index = current.findIndex(r => r.id === id);
        if (index !== -1) {
          current[index] = updatedRecipe;
          this.recipes$.next([...current]);
        }
      })
    );
  }
  
  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipes/${id}`).pipe(
      tap(() => {
        const current = this.recipes$.value;
        this.recipes$.next(current.filter(r => r.id !== id));
      })
    );
  }
  
  uploadPhoto(file: File): Observable<{ fileId: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ fileId: string }>(`${this.baseUrl}/recipes/upload-photo`, formData);
  }
}
```

- [ ] **Шаг 4: Создать MealPlanService**

```typescript
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { MealPlanEntry, CreateMealPlanEntryRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MealPlanService extends ApiService {
  
  private mealPlan$ = new BehaviorSubject<MealPlanEntry[]>([]);
  
  getMealPlanObservable(): Observable<MealPlanEntry[]> {
    return this.mealPlan$.asObservable();
  }
  
  loadMealPlan(weekStart: string): Observable<MealPlanEntry[]> {
    return this.http.get<MealPlanEntry[]>(`${this.baseUrl}/meal-plan`, {
      params: { weekStart }
    }).pipe(
      tap(entries => this.mealPlan$.next(entries))
    );
  }
  
  addMealPlanEntry(request: CreateMealPlanEntryRequest): Observable<MealPlanEntry> {
    return this.http.post<MealPlanEntry>(`${this.baseUrl}/meal-plan`, request).pipe(
      tap(entry => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next([...current, entry]);
      })
    );
  }
  
  deleteMealPlanEntry(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/meal-plan/${id}`).pipe(
      tap(() => {
        const current = this.mealPlan$.value;
        this.mealPlan$.next(current.filter(e => e.id !== id));
      })
    );
  }
}
```

- [ ] **Шаг 5: Создать ShoppingListService**

```typescript
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ShoppingListItem, CreateManualItemRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends ApiService {
  
  private shoppingList$ = new BehaviorSubject<ShoppingListItem[]>([]);
  
  getShoppingListObservable(): Observable<ShoppingListItem[]> {
    return this.shoppingList$.asObservable();
  }
  
  loadShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.http.get<ShoppingListItem[]>(`${this.baseUrl}/shopping-list`, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }
  
  regenerateShoppingList(weekStart: string): Observable<ShoppingListItem[]> {
    return this.http.post<ShoppingListItem[]>(`${this.baseUrl}/shopping-list/regenerate`, {}, {
      params: { weekStart }
    }).pipe(
      tap(items => this.shoppingList$.next(items))
    );
  }
  
  addManualItem(request: CreateManualItemRequest, weekStart: string): Observable<ShoppingListItem> {
    return this.http.post<ShoppingListItem>(`${this.baseUrl}/shopping-list/items`, request, {
      params: { weekStart }
    }).pipe(
      tap(item => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next([...current, item]);
      })
    );
  }
  
  toggleItemChecked(id: number): Observable<ShoppingListItem> {
    return this.http.patch<ShoppingListItem>(`${this.baseUrl}/shopping-list/items/${id}`, {}).pipe(
      tap(updatedItem => {
        const current = this.shoppingList$.value;
        const index = current.findIndex(i => i.id === id);
        if (index !== -1) {
          current[index] = updatedItem;
          this.shoppingList$.next([...current]);
        }
      })
    );
  }
  
  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/shopping-list/items/${id}`).pipe(
      tap(() => {
        const current = this.shoppingList$.value;
        this.shoppingList$.next(current.filter(i => i.id !== id));
      })
    );
  }
}
```

- [ ] **Шаг 6: Коммит**

```bash
git add frontend/src/app/shared/services/
git commit -m "feat: add API services for Couple, Recipe, MealPlan, and ShoppingList"
```

---

### Задача 3.7: Настроить роутинг

**Файлы:**
- Изменить: `frontend/src/app/app.routes.ts`

- [ ] **Шаг 1: Обновить app.routes.ts**

Заменить содержимое `frontend/src/app/app.routes.ts`:

```typescript
import { Routes } from '@angular/router';
import { coupleGuard } from './core/guards/couple.guard';

export const routes: Routes = [
  {
    path: 'join',
    loadComponent: () => import('./features/couple/couple-join/couple-join.component')
      .then(m => m.CoupleJoinComponent)
  },
  {
    path: '',
    loadComponent: () => import('./features/home/home.component')
      .then(m => m.HomeComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes',
    loadComponent: () => import('./features/recipes/recipe-list/recipe-list.component')
      .then(m => m.RecipeListComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/new',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/:id',
    loadComponent: () => import('./features/recipes/recipe-detail/recipe-detail.component')
      .then(m => m.RecipeDetailComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'recipes/:id/edit',
    loadComponent: () => import('./features/recipes/recipe-form/recipe-form.component')
      .then(m => m.RecipeFormComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'plan',
    loadComponent: () => import('./features/meal-plan/meal-plan.component')
      .then(m => m.MealPlanComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'shopping',
    loadComponent: () => import('./features/shopping-list/shopping-list.component')
      .then(m => m.ShoppingListComponent),
    canActivate: [coupleGuard]
  },
  {
    path: 'couple',
    loadComponent: () => import('./features/couple/couple-profile/couple-profile.component')
      .then(m => m.CoupleProfileComponent),
    canActivate: [coupleGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
```

- [ ] **Шаг 2: Коммит**

```bash
git add frontend/src/app/app.routes.ts
git commit -m "feat: configure routing with lazy loading and CoupleGuard"
```

---

### Задача 3.8: Создать компонент CoupleJoin

**Файлы:**
- Создать: `frontend/src/app/features/couple/couple-join/couple-join.component.ts`
- Создать: `frontend/src/app/features/couple/couple-join/couple-join.component.html`
- Создать: `frontend/src/app/features/couple/couple-join/couple-join.component.scss`

- [ ] **Шаг 1: Создать couple-join.component.ts**

```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';

@Component({
  selector: 'app-couple-join',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './couple-join.component.html',
  styleUrl: './couple-join.component.scss'
})
export class CoupleJoinComponent {
  mode: 'initial' | 'create' | 'join' = 'initial';
  inviteCode = '';
  generatedCode = '';
  error = '';
  
  constructor(
    private coupleService: CoupleService,
    private telegramService: TelegramService,
    private router: Router
  ) {}
  
  createCouple() {
    this.coupleService.createCouple().subscribe({
      next: (couple) => {
        this.generatedCode = couple.inviteCode;
        this.mode = 'create';
      },
      error: (err) => {
        this.error = 'Не удалось создать пару';
      }
    });
  }
  
  joinCouple() {
    if (this.inviteCode.length !== 6) {
      this.error = 'Код должен содержать 6 символов';
      return;
    }
    
    this.coupleService.joinCouple({ inviteCode: this.inviteCode.toUpperCase() }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = 'Неверный код приглашения';
      }
    });
  }
  
  shareCode() {
    const url = `https://t.me/your_bot?start=${this.generatedCode}`;
    this.telegramService.share(url, 'Присоединяйся к планировщику питания!');
  }
  
  showJoinMode() {
    this.mode = 'join';
    this.error = '';
  }
  
  backToInitial() {
    this.mode = 'initial';
    this.error = '';
    this.inviteCode = '';
  }
}
```

- [ ] **Шаг 2: Создать couple-join.component.html**

```html
<div class="container">
  <h1 class="title">Планировщик питания</h1>
  
  @if (mode === 'initial') {
    <div class="buttons">
      <button class="btn btn-primary" (click)="createCouple()">
        Создать пару
      </button>
      <button class="btn btn-secondary" (click)="showJoinMode()">
        Ввести код
      </button>
    </div>
  }
  
  @if (mode === 'create') {
    <div class="code-section">
      <p class="subtitle">Поделитесь кодом с партнёром:</p>
      <div class="code-display">{{ generatedCode }}</div>
      <button class="btn btn-primary" (click)="shareCode()">
        Поделиться
      </button>
    </div>
  }
  
  @if (mode === 'join') {
    <div class="join-section">
      <button class="btn-back" (click)="backToInitial()">← Назад</button>
      <input
        type="text"
        class="code-input"
        [(ngModel)]="inviteCode"
        placeholder="Введите 6-значный код"
        maxlength="6"
        (keyup.enter)="joinCouple()"
      />
      <button class="btn btn-primary" (click)="joinCouple()">
        Присоединиться
      </button>
    </div>
  }
  
  @if (error) {
    <div class="error">{{ error }}</div>
  }
</div>
```

- [ ] **Шаг 3: Создать couple-join.component.scss**

```scss
.container {
  padding: 24px;
  text-align: center;
}

.title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 48px;
}

.buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.btn {
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.7;
  }
}

.btn-primary {
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
}

.btn-secondary {
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  color: var(--tg-theme-text-color, #000);
}

.code-section {
  margin-top: 24px;
}

.subtitle {
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  margin-bottom: 16px;
}

.code-display {
  font-size: 36px;
  font-weight: bold;
  letter-spacing: 8px;
  padding: 24px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 12px;
  margin-bottom: 24px;
}

.join-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.btn-back {
  align-self: flex-start;
  background: none;
  border: none;
  color: var(--tg-theme-link-color, #4fc3f7);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
}

.code-input {
  padding: 16px;
  font-size: 24px;
  text-align: center;
  letter-spacing: 8px;
  border: 2px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 12px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.error {
  margin-top: 16px;
  padding: 12px;
  background: #ff5252;
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/couple/couple-join/
git commit -m "feat: add CoupleJoin component with create and join functionality"
```

---

**Продолжение плана следует в следующем сообщении из-за ограничения размера...**

План получился очень объёмным. Я создал детальные задачи для:
- **Фаза 1:** Backend модель данных для пар (10 задач) ✓
- **Фаза 2:** Backend загрузка фото (3 задачи) ✓
- **Фаза 3:** Frontend базовая структура (8 задач) ✓

Осталось создать:
- Фаза 4: Frontend — Рецепты
- Фаза 5: Frontend — План на неделю
- Фаза 6: Frontend — Список покупок
- Фаза 7: Frontend — Главная и профиль
- Фаза 8: Тестирование и полировка

---

## Фаза 4: Frontend — Рецепты

### Задача 4.1: Создать компонент RecipeCard

**Файлы:**
- Создать: `frontend/src/app/shared/components/recipe-card/recipe-card.component.ts`
- Создать: `frontend/src/app/shared/components/recipe-card/recipe-card.component.html`
- Создать: `frontend/src/app/shared/components/recipe-card/recipe-card.component.scss`

- [ ] **Шаг 1: Создать recipe-card.component.ts**

```typescript
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Recipe } from '../../models';

@Component({
  selector: 'app-recipe-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recipe-card.component.html',
  styleUrl: './recipe-card.component.scss'
})
export class RecipeCardComponent {
  @Input() recipe!: Recipe;
  
  constructor(private router: Router) {}
  
  onClick() {
    this.router.navigate(['/recipes', this.recipe.id]);
  }
}
```

- [ ] **Шаг 2: Создать recipe-card.component.html**

```html
<div class="recipe-card" (click)="onClick()">
  <div class="image-container">
    @if (recipe.photoUrl) {
      <img [src]="'/api/recipes/photo/' + recipe.photoUrl" [alt]="recipe.name" class="recipe-image" />
    } @else {
      <div class="placeholder-image">📷</div>
    }
  </div>
  <div class="recipe-name">{{ recipe.name }}</div>
</div>
```

- [ ] **Шаг 3: Создать recipe-card.component.scss**

```scss
.recipe-card {
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.98);
  }
}

.image-container {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.recipe-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: var(--tg-theme-bg-color, #fff);
}

.recipe-name {
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  color: var(--tg-theme-text-color, #000);
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/shared/components/recipe-card/
git commit -m "feat: add RecipeCard component for displaying recipe in grid"
```

---

### Задача 4.2: Создать компонент RecipeList

**Файлы:**
- Создать: `frontend/src/app/features/recipes/recipe-list/recipe-list.component.ts`
- Создать: `frontend/src/app/features/recipes/recipe-list/recipe-list.component.html`
- Создать: `frontend/src/app/features/recipes/recipe-list/recipe-list.component.scss`

- [ ] **Шаг 1: Создать recipe-list.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { RecipeService } from '../../../shared/services/recipe.service';
import { RecipeCardComponent } from '../../../shared/components/recipe-card/recipe-card.component';
import { Recipe } from '../../../shared/models';

@Component({
  selector: 'app-recipe-list',
  standalone: true,
  imports: [CommonModule, RecipeCardComponent],
  templateUrl: './recipe-list.component.html',
  styleUrl: './recipe-list.component.scss'
})
export class RecipeListComponent implements OnInit {
  recipes: Recipe[] = [];
  
  constructor(
    private recipeService: RecipeService,
    private router: Router
  ) {}
  
  ngOnInit() {
    this.recipeService.loadRecipes().subscribe({
      next: (recipes) => {
        this.recipes = recipes;
      }
    });
  }
  
  addRecipe() {
    this.router.navigate(['/recipes/new']);
  }
}
```

- [ ] **Шаг 2: Создать recipe-list.component.html**

```html
<div class="container">
  <h1 class="title">Рецепты</h1>
  
  <div class="recipe-grid">
    @for (recipe of recipes; track recipe.id) {
      <app-recipe-card [recipe]="recipe" />
    }
  </div>
  
  @if (recipes.length === 0) {
    <div class="empty-state">
      <p>Пока нет рецептов</p>
      <p class="subtitle">Добавьте первый рецепт!</p>
    </div>
  }
  
  <button class="fab" (click)="addRecipe()">+</button>
</div>
```

- [ ] **Шаг 3: Создать recipe-list.component.scss**

```scss
.container {
  padding: 16px;
  padding-bottom: 80px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 16px;
  color: var(--tg-theme-text-color, #000);
}

.recipe-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--tg-theme-hint-color, #888);
  
  p {
    margin: 8px 0;
  }
  
  .subtitle {
    font-size: 14px;
  }
}

.fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  font-size: 32px;
  font-weight: 300;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.95);
  }
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/recipes/recipe-list/
git commit -m "feat: add RecipeList component with grid layout and FAB"
```

---

### Задача 4.3: Создать компонент RecipeDetail

**Файлы:**
- Создать: `frontend/src/app/features/recipes/recipe-detail/recipe-detail.component.ts`
- Создать: `frontend/src/app/features/recipes/recipe-detail/recipe-detail.component.html`
- Создать: `frontend/src/app/features/recipes/recipe-detail/recipe-detail.component.scss`
- Создать: `frontend/src/app/shared/components/modal/modal.component.ts`
- Создать: `frontend/src/app/shared/components/modal/modal.component.html`
- Создать: `frontend/src/app/shared/components/modal/modal.component.scss`

- [ ] **Шаг 1: Создать modal.component.ts**

```typescript
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss'
})
export class ModalComponent {
  @Input() isOpen = false;
  @Input() title = '';
  @Output() close = new EventEmitter<void>();
  
  onClose() {
    this.close.emit();
  }
  
  onBackdropClick(event: Event) {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.onClose();
    }
  }
}
```

- [ ] **Шаг 2: Создать modal.component.html**

```html
@if (isOpen) {
  <div class="modal-backdrop" (click)="onBackdropClick($event)">
    <div class="modal-content">
      <div class="modal-header">
        <h2 class="modal-title">{{ title }}</h2>
        <button class="close-btn" (click)="onClose()">×</button>
      </div>
      <div class="modal-body">
        <ng-content />
      </div>
    </div>
  </div>
}
```

- [ ] **Шаг 3: Создать modal.component.scss**

```scss
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.modal-content {
  background: var(--tg-theme-bg-color, #fff);
  border-radius: 16px;
  max-width: 400px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--tg-theme-hint-color, #eee);
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: var(--tg-theme-text-color, #000);
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
  color: var(--tg-theme-hint-color, #888);
  padding: 0;
  width: 32px;
  height: 32px;
}

.modal-body {
  padding: 16px;
}
```

- [ ] **Шаг 4: Создать recipe-detail.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipeService } from '../../../shared/services/recipe.service';
import { MealPlanService } from '../../../shared/services/meal-plan.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Recipe, MealPlanEntry } from '../../../shared/models';

@Component({
  selector: 'app-recipe-detail',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './recipe-detail.component.html',
  styleUrl: './recipe-detail.component.scss'
})
export class RecipeDetailComponent implements OnInit {
  recipe: Recipe | null = null;
  activeTab: 'ingredients' | 'instructions' = 'ingredients';
  showAddToPlanModal = false;
  
  selectedDays: boolean[] = [false, false, false, false, false, false, false];
  selectedMealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER' = 'BREAKFAST';
  
  dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
  mealTypes = [
    { value: 'BREAKFAST', label: 'Завтрак' },
    { value: 'LUNCH', label: 'Обед' },
    { value: 'AFTERNOON_SNACK', label: 'Полдник' },
    { value: 'DINNER', label: 'Ужин' }
  ];
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private mealPlanService: MealPlanService,
    private telegramService: TelegramService
  ) {}
  
  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.recipeService.getRecipe(id).subscribe({
      next: (recipe) => {
        this.recipe = recipe;
      }
    });
    
    this.telegramService.showBackButton(() => {
      this.router.navigate(['/recipes']);
    });
  }
  
  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }
  
  setTab(tab: 'ingredients' | 'instructions') {
    this.activeTab = tab;
  }
  
  editRecipe() {
    if (this.recipe) {
      this.router.navigate(['/recipes', this.recipe.id, 'edit']);
    }
  }
  
  deleteRecipe() {
    if (this.recipe && confirm('Удалить рецепт?')) {
      this.recipeService.deleteRecipe(this.recipe.id).subscribe({
        next: () => {
          this.router.navigate(['/recipes']);
        }
      });
    }
  }
  
  openAddToPlanModal() {
    this.showAddToPlanModal = true;
    this.selectedDays = [false, false, false, false, false, false, false];
    this.selectedMealType = 'BREAKFAST';
  }
  
  closeAddToPlanModal() {
    this.showAddToPlanModal = false;
  }
  
  addToPlan() {
    if (!this.recipe) return;
    
    const today = new Date();
    const dayOfWeek = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    
    this.selectedDays.forEach((selected, index) => {
      if (selected) {
        const date = new Date(monday);
        date.setDate(monday.getDate() + index);
        const dateStr = date.toISOString().split('T')[0];
        
        this.mealPlanService.addMealPlanEntry({
          date: dateStr,
          recipeId: this.recipe!.id,
          mealType: this.selectedMealType
        }).subscribe();
      }
    });
    
    this.closeAddToPlanModal();
    this.telegramService.hapticFeedback('medium');
  }
}
```

- [ ] **Шаг 5: Создать recipe-detail.component.html**

```html
@if (recipe) {
  <div class="container">
    <div class="image-container">
      @if (recipe.photoUrl) {
        <img [src]="'/api/recipes/photo/' + recipe.photoUrl" [alt]="recipe.name" class="recipe-image" />
      } @else {
        <div class="placeholder-image">📷</div>
      }
    </div>
    
    <div class="actions">
      <button class="icon-btn" (click)="editRecipe()">✏️</button>
      <button class="icon-btn" (click)="deleteRecipe()">🗑️</button>
    </div>
    
    <h1 class="recipe-name">{{ recipe.name }}</h1>
    <p class="recipe-description">{{ recipe.description }}</p>
    
    <div class="tabs">
      <button 
        class="tab" 
        [class.active]="activeTab === 'ingredients'"
        (click)="setTab('ingredients')">
        Ингредиенты
      </button>
      <button 
        class="tab" 
        [class.active]="activeTab === 'instructions'"
        (click)="setTab('instructions')">
        Приготовление
      </button>
    </div>
    
    <div class="tab-content">
      @if (activeTab === 'ingredients') {
        <ul class="ingredients-list">
          @for (ingredient of recipe.ingredients; track ingredient.id) {
            <li class="ingredient-item">
              <span class="ingredient-name">{{ ingredient.name }}</span>
              <span class="ingredient-amount">{{ ingredient.weightInGrams }} {{ ingredient.unit === 'GRAM' ? 'г' : ingredient.unit === 'MILLILITER' ? 'мл' : 'шт' }}</span>
            </li>
          }
        </ul>
      } @else {
        <div class="instructions">{{ recipe.instructions }}</div>
      }
    </div>
    
    <button class="btn-add-to-plan" (click)="openAddToPlanModal()">
      + Добавить в план
    </button>
  </div>
  
  <app-modal [isOpen]="showAddToPlanModal" title="Добавить в план" (close)="closeAddToPlanModal()">
    <div class="modal-form">
      <div class="form-section">
        <label class="form-label">Выберите дни:</label>
        <div class="days-grid">
          @for (day of dayNames; track day; let i = $index) {
            <label class="day-checkbox">
              <input type="checkbox" [(ngModel)]="selectedDays[i]" />
              <span>{{ day }}</span>
            </label>
          }
        </div>
      </div>
      
      <div class="form-section">
        <label class="form-label">Приём пищи:</label>
        <select [(ngModel)]="selectedMealType" class="form-select">
          @for (type of mealTypes; track type.value) {
            <option [value]="type.value">{{ type.label }}</option>
          }
        </select>
      </div>
      
      <button class="btn-primary" (click)="addToPlan()">Добавить</button>
    </div>
  </app-modal>
}
```

- [ ] **Шаг 6: Создать recipe-detail.component.scss**

```scss
.container {
  padding-bottom: 80px;
}

.image-container {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
}

.recipe-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
}

.actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recipe-name {
  padding: 16px 16px 8px;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: var(--tg-theme-text-color, #000);
}

.recipe-description {
  padding: 0 16px 16px;
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  margin: 0;
}

.tabs {
  display: flex;
  border-bottom: 1px solid var(--tg-theme-hint-color, #eee);
  margin: 0 16px;
}

.tab {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-hint-color, #888);
  cursor: pointer;
  transition: all 0.2s;
  
  &.active {
    color: var(--tg-theme-button-color, #4fc3f7);
    border-bottom-color: var(--tg-theme-button-color, #4fc3f7);
  }
}

.tab-content {
  padding: 16px;
}

.ingredients-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ingredient-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--tg-theme-hint-color, #eee);
  
  &:last-child {
    border-bottom: none;
  }
}

.ingredient-name {
  color: var(--tg-theme-text-color, #000);
}

.ingredient-amount {
  color: var(--tg-theme-hint-color, #888);
  font-weight: 600;
}

.instructions {
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--tg-theme-text-color, #000);
}

.btn-add-to-plan {
  position: fixed;
  bottom: 24px;
  left: 16px;
  right: 16px;
  padding: 16px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.day-checkbox {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  cursor: pointer;
  
  input {
    cursor: pointer;
  }
}

.form-select {
  padding: 12px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.btn-primary {
  padding: 12px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
```

- [ ] **Шаг 7: Коммит**

```bash
git add frontend/src/app/shared/components/modal/
git add frontend/src/app/features/recipes/recipe-detail/
git commit -m "feat: add RecipeDetail component with tabs and add-to-plan modal"
```

---

### Задача 4.4: Создать компонент RecipeForm

**Файлы:**
- Создать: `frontend/src/app/features/recipes/recipe-form/recipe-form.component.ts`
- Создать: `frontend/src/app/features/recipes/recipe-form/recipe-form.component.html`
- Создать: `frontend/src/app/features/recipes/recipe-form/recipe-form.component.scss`

- [ ] **Шаг 1: Создать recipe-form.component.ts**

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipeService } from '../../../shared/services/recipe.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { Recipe, IngredientRequest } from '../../../shared/models';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss'
})
export class RecipeFormComponent implements OnInit, OnDestroy {
  isEditMode = false;
  recipeId: number | null = null;
  
  name = '';
  description = '';
  photoUrl = '';
  instructions = '';
  ingredients: IngredientRequest[] = [];
  
  units = [
    { value: 'GRAM', label: 'г' },
    { value: 'MILLILITER', label: 'мл' },
    { value: 'PIECE', label: 'шт' }
  ];
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private telegramService: TelegramService
  ) {}
  
  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.recipeId = Number(id);
      this.recipeService.getRecipe(this.recipeId).subscribe({
        next: (recipe) => {
          this.name = recipe.name;
          this.description = recipe.description;
          this.photoUrl = recipe.photoUrl;
          this.instructions = recipe.instructions;
          this.ingredients = recipe.ingredients.map(i => ({
            name: i.name,
            weightInGrams: i.weightInGrams,
            unit: i.unit
          }));
        }
      });
    }
    
    this.telegramService.showBackButton(() => {
      this.router.navigate(['/recipes']);
    });
    
    this.telegramService.showMainButton('Сохранить', () => {
      this.saveRecipe();
    });
  }
  
  ngOnDestroy() {
    this.telegramService.hideBackButton();
    this.telegramService.hideMainButton();
  }
  
  addIngredient() {
    this.ingredients.push({
      name: '',
      weightInGrams: 0,
      unit: 'GRAM'
    });
  }
  
  removeIngredient(index: number) {
    this.ingredients.splice(index, 1);
  }
  
  addPhoto() {
    this.telegramService.openGallery((photo) => {
      const file = this.dataURLtoFile(photo, 'photo.jpg');
      this.recipeService.uploadPhoto(file).subscribe({
        next: (response) => {
          this.photoUrl = response.fileId;
        }
      });
    });
  }
  
  private dataURLtoFile(dataurl: string, filename: string): File {
    const arr = dataurl.split(',');
    const mime = arr[0].match(/:(.*?);/)?.[1] || 'image/jpeg';
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], filename, { type: mime });
  }
  
  saveRecipe() {
    const request = {
      name: this.name,
      description: this.description,
      photoUrl: this.photoUrl,
      instructions: this.instructions,
      ingredients: this.ingredients
    };
    
    if (this.isEditMode && this.recipeId) {
      this.recipeService.updateRecipe(this.recipeId, request).subscribe({
        next: () => {
          this.router.navigate(['/recipes', this.recipeId]);
        }
      });
    } else {
      this.recipeService.createRecipe(request).subscribe({
        next: (recipe) => {
          this.router.navigate(['/recipes', recipe.id]);
        }
      });
    }
  }
}
```

- [ ] **Шаг 2: Создать recipe-form.component.html**

```html
<div class="container">
  <h1 class="title">{{ isEditMode ? 'Редактировать рецепт' : 'Новый рецепт' }}</h1>
  
  <div class="form-group">
    <label class="form-label">Название</label>
    <input type="text" class="form-input" [(ngModel)]="name" placeholder="Например: Борщ" />
  </div>
  
  <div class="form-group">
    <label class="form-label">Описание</label>
    <textarea class="form-textarea" [(ngModel)]="description" placeholder="Краткое описание блюда" rows="3"></textarea>
  </div>
  
  <div class="form-group">
    <label class="form-label">Фото</label>
    @if (photoUrl) {
      <div class="photo-preview">
        <img [src]="'/api/recipes/photo/' + photoUrl" alt="Preview" />
        <button class="btn-change-photo" (click)="addPhoto()">Изменить фото</button>
      </div>
    } @else {
      <button class="btn-add-photo" (click)="addPhoto()">+ Добавить фото</button>
    }
  </div>
  
  <div class="form-group">
    <label class="form-label">Ингредиенты</label>
    @for (ingredient of ingredients; track $index; let i = $index) {
      <div class="ingredient-row">
        <input type="text" class="form-input ingredient-name" [(ngModel)]="ingredient.name" placeholder="Название" />
        <input type="number" class="form-input ingredient-amount" [(ngModel)]="ingredient.weightInGrams" placeholder="0" />
        <select class="form-select ingredient-unit" [(ngModel)]="ingredient.unit">
          @for (unit of units; track unit.value) {
            <option [value]="unit.value">{{ unit.label }}</option>
          }
        </select>
        <button class="btn-remove" (click)="removeIngredient(i)">×</button>
      </div>
    }
    <button class="btn-add-ingredient" (click)="addIngredient()">+ Ингредиент</button>
  </div>
  
  <div class="form-group">
    <label class="form-label">Инструкция приготовления</label>
    <textarea class="form-textarea" [(ngModel)]="instructions" placeholder="Пошаговая инструкция" rows="8"></textarea>
  </div>
</div>
```

- [ ] **Шаг 3: Создать recipe-form.component.scss**

```scss
.container {
  padding: 16px;
  padding-bottom: 80px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
  color: var(--tg-theme-text-color, #000);
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--tg-theme-text-color, #000);
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
  resize: vertical;
  font-family: inherit;
}

.photo-preview {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.btn-change-photo {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
}

.btn-add-photo {
  width: 100%;
  padding: 32px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border: 2px dashed var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  cursor: pointer;
}

.ingredient-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr auto;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.ingredient-name,
.ingredient-amount,
.ingredient-unit {
  padding: 8px;
  font-size: 12px;
}

.form-select {
  padding: 8px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 12px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.btn-remove {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #ff5252;
  color: #fff;
  border: none;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.btn-add-ingredient {
  width: 100%;
  padding: 12px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border: 1px dashed var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  cursor: pointer;
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/recipes/recipe-form/
git commit -m "feat: add RecipeForm component for creating and editing recipes"
```

---

**Продолжение следует...**

Я создал детальные задачи для Фазы 4 (Рецепты). Осталось создать:
- Фаза 5: Frontend — План на неделю
- Фаза 6: Frontend — Список покупок
- Фаза 7: Frontend — Главная и профиль
- Фаза 8: Тестирование и полировка

---

## Фаза 5: Frontend — План на неделю

### Задача 5.1: Создать компонент DayCard

**Файлы:**
- Создать: `frontend/src/app/shared/components/day-card/day-card.component.ts`
- Создать: `frontend/src/app/shared/components/day-card/day-card.component.html`
- Создать: `frontend/src/app/shared/components/day-card/day-card.component.scss`

- [ ] **Шаг 1: Создать day-card.component.ts**

```typescript
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MealPlanEntry } from '../../models';

@Component({
  selector: 'app-day-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './day-card.component.html',
  styleUrl: './day-card.component.scss'
})
export class DayCardComponent {
  @Input() date!: string;
  @Input() dayName!: string;
  @Input() entries: MealPlanEntry[] = [];
  @Output() slotClick = new EventEmitter<{ date: string; mealType: string }>();
  
  mealTypes = [
    { type: 'BREAKFAST', label: 'Завтрак', icon: '🌅' },
    { type: 'LUNCH', label: 'Обед', icon: '☀️' },
    { type: 'AFTERNOON_SNACK', label: 'Полдник', icon: '🍪' },
    { type: 'DINNER', label: 'Ужин', icon: '🌙' }
  ];
  
  getRecipeForMeal(mealType: string): string {
    const entry = this.entries.find(e => e.mealType === mealType);
    return entry ? entry.recipe.name : '—';
  }
  
  onSlotClick(mealType: string) {
    this.slotClick.emit({ date: this.date, mealType });
  }
}
```

- [ ] **Шаг 2: Создать day-card.component.html**

```html
<div class="day-card">
  <div class="day-header">{{ dayName }}, {{ date | date:'d MMM':'ru' }}</div>
  
  <div class="meal-slots">
    @for (meal of mealTypes; track meal.type) {
      <div class="meal-slot" (click)="onSlotClick(meal.type)">
        <span class="meal-icon">{{ meal.icon }}</span>
        <span class="meal-label">{{ meal.label }}:</span>
        <span class="meal-recipe" [class.empty]="getRecipeForMeal(meal.type) === '—'">
          {{ getRecipeForMeal(meal.type) }}
        </span>
      </div>
    }
  </div>
</div>
```

- [ ] **Шаг 3: Создать day-card.component.scss**

```scss
.day-card {
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.day-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  color: var(--tg-theme-text-color, #000);
}

.meal-slots {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meal-slot {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--tg-theme-bg-color, #fff);
  border-radius: 8px;
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.7;
  }
}

.meal-icon {
  font-size: 16px;
}

.meal-label {
  font-size: 12px;
  color: var(--tg-theme-hint-color, #888);
  min-width: 70px;
}

.meal-recipe {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
  
  &.empty {
    color: var(--tg-theme-hint-color, #ccc);
    font-weight: normal;
  }
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/shared/components/day-card/
git commit -m "feat: add DayCard component for displaying day in meal plan"
```

---

### Задача 5.2: Создать компонент MealPlan

**Файлы:**
- Создать: `frontend/src/app/features/meal-plan/meal-plan.component.ts`
- Создать: `frontend/src/app/features/meal-plan/meal-plan.component.html`
- Создать: `frontend/src/app/features/meal-plan/meal-plan.component.scss`

- [ ] **Шаг 1: Создать meal-plan.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MealPlanService } from '../../shared/services/meal-plan.service';
import { RecipeService } from '../../shared/services/recipe.service';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { DayCardComponent } from '../../shared/components/day-card/day-card.component';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { MealPlanEntry, Recipe } from '../../shared/models';

@Component({
  selector: 'app-meal-plan',
  standalone: true,
  imports: [CommonModule, DayCardComponent, ModalComponent],
  templateUrl: './meal-plan.component.html',
  styleUrl: './meal-plan.component.scss'
})
export class MealPlanComponent implements OnInit {
  weekStart: Date = new Date();
  days: { date: string; dayName: string; entries: MealPlanEntry[] }[] = [];
  recipes: Recipe[] = [];
  
  showRecipeModal = false;
  selectedDate = '';
  selectedMealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER' = 'BREAKFAST';
  
  dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
  
  constructor(
    private router: Router,
    private mealPlanService: MealPlanService,
    private recipeService: RecipeService,
    private shoppingListService: ShoppingListService,
    private telegramService: TelegramService
  ) {}
  
  ngOnInit() {
    this.setWeekStart(new Date());
    this.loadRecipes();
    
    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }
  
  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }
  
  loadRecipes() {
    this.recipeService.loadRecipes().subscribe({
      next: (recipes) => {
        this.recipes = recipes;
      }
    });
  }
  
  setWeekStart(date: Date) {
    const dayOfWeek = date.getDay();
    const monday = new Date(date);
    monday.setDate(date.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    this.weekStart = monday;
    this.loadWeek();
  }
  
  loadWeek() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.mealPlanService.loadMealPlan(weekStartStr).subscribe({
      next: (entries) => {
        this.days = [];
        for (let i = 0; i < 7; i++) {
          const date = new Date(this.weekStart);
          date.setDate(this.weekStart.getDate() + i);
          const dateStr = date.toISOString().split('T')[0];
          const dayEntries = entries.filter(e => e.date === dateStr);
          this.days.push({
            date: dateStr,
            dayName: this.dayNames[i],
            entries: dayEntries
          });
        }
      }
    });
  }
  
  previousWeek() {
    const prevWeek = new Date(this.weekStart);
    prevWeek.setDate(this.weekStart.getDate() - 7);
    this.setWeekStart(prevWeek);
  }
  
  nextWeek() {
    const nextWeek = new Date(this.weekStart);
    nextWeek.setDate(this.weekStart.getDate() + 7);
    this.setWeekStart(nextWeek);
  }
  
  onSlotClick(event: { date: string; mealType: string }) {
    const existingEntry = this.days
      .find(d => d.date === event.date)?.entries
      .find(e => e.mealType === event.mealType);
    
    if (existingEntry) {
      if (confirm('Удалить из плана?')) {
        this.mealPlanService.deleteMealPlanEntry(existingEntry.id).subscribe({
          next: () => {
            this.loadWeek();
          }
        });
      }
    } else {
      this.selectedDate = event.date;
      this.selectedMealType = event.mealType as any;
      this.showRecipeModal = true;
    }
  }
  
  closeRecipeModal() {
    this.showRecipeModal = false;
  }
  
  selectRecipe(recipe: Recipe) {
    this.mealPlanService.addMealPlanEntry({
      date: this.selectedDate,
      recipeId: recipe.id,
      mealType: this.selectedMealType
    }).subscribe({
      next: () => {
        this.closeRecipeModal();
        this.loadWeek();
        this.telegramService.hapticFeedback('light');
      }
    });
  }
  
  generateShoppingList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.regenerateShoppingList(weekStartStr).subscribe({
      next: () => {
        this.router.navigate(['/shopping']);
      }
    });
  }
  
  getWeekRange(): string {
    const end = new Date(this.weekStart);
    end.setDate(this.weekStart.getDate() + 6);
    const startStr = this.weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const endStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    return `${startStr} — ${endStr}`;
  }
}
```

- [ ] **Шаг 2: Создать meal-plan.component.html**

```html
<div class="container">
  <h1 class="title">План на неделю</h1>
  <div class="week-range">{{ getWeekRange() }}</div>
  
  <div class="week-nav">
    <button class="nav-btn" (click)="previousWeek()">← Пред.</button>
    <button class="nav-btn" (click)="nextWeek()">След. →</button>
  </div>
  
  <div class="days-list">
    @for (day of days; track day.date) {
      <app-day-card
        [date]="day.date"
        [dayName]="day.dayName"
        [entries]="day.entries"
        (slotClick)="onSlotClick($event)"
      />
    }
  </div>
  
  <button class="btn-generate" (click)="generateShoppingList()">
    Сгенерировать список покупок
  </button>
</div>

<app-modal [isOpen]="showRecipeModal" title="Выберите рецепт" (close)="closeRecipeModal()">
  <div class="recipe-list">
    @for (recipe of recipes; track recipe.id) {
      <div class="recipe-option" (click)="selectRecipe(recipe)">
        {{ recipe.name }}
      </div>
    }
    @if (recipes.length === 0) {
      <div class="empty-state">Нет рецептов</div>
    }
  </div>
</app-modal>
```

- [ ] **Шаг 3: Создать meal-plan.component.scss**

```scss
.container {
  padding: 16px;
  padding-bottom: 100px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
  color: var(--tg-theme-text-color, #000);
}

.week-range {
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  margin-bottom: 16px;
}

.week-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.nav-btn {
  flex: 1;
  padding: 10px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: var(--tg-theme-text-color, #000);
  cursor: pointer;
}

.days-list {
  margin-bottom: 16px;
}

.btn-generate {
  position: fixed;
  bottom: 24px;
  left: 16px;
  right: 16px;
  padding: 16px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.recipe-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recipe-option {
  padding: 12px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 8px;
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.7;
  }
}

.empty-state {
  text-align: center;
  padding: 24px;
  color: var(--tg-theme-hint-color, #888);
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/meal-plan/
git commit -m "feat: add MealPlan component with week navigation and recipe selection"
```

---

## Фаза 6: Frontend — Список покупок

### Задача 6.1: Создать компонент ShoppingList

**Файлы:**
- Создать: `frontend/src/app/features/shopping-list/shopping-list.component.ts`
- Создать: `frontend/src/app/features/shopping-list/shopping-list.component.html`
- Создать: `frontend/src/app/features/shopping-list/shopping-list.component.scss`

- [ ] **Шаг 1: Создать shopping-list.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShoppingListService } from '../../shared/services/shopping-list.service';
import { TelegramService } from '../../core/services/telegram.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { ShoppingListItem } from '../../shared/models';

@Component({
  selector: 'app-shopping-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './shopping-list.component.html',
  styleUrl: './shopping-list.component.scss'
})
export class ShoppingListComponent implements OnInit {
  weekStart: Date = new Date();
  items: ShoppingListItem[] = [];
  
  showAddModal = false;
  newItemName = '';
  newItemQuantity = 0;
  newItemUnit: 'GRAM' | 'MILLILITER' | 'PIECE' = 'GRAM';
  
  units = [
    { value: 'GRAM', label: 'г' },
    { value: 'MILLILITER', label: 'мл' },
    { value: 'PIECE', label: 'шт' }
  ];
  
  constructor(
    private router: Router,
    private shoppingListService: ShoppingListService,
    private telegramService: TelegramService
  ) {}
  
  ngOnInit() {
    const dayOfWeek = this.weekStart.getDay();
    const monday = new Date(this.weekStart);
    monday.setDate(this.weekStart.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    this.weekStart = monday;
    this.loadShoppingList();
    
    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }
  
  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }
  
  loadShoppingList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.loadShoppingList(weekStartStr).subscribe({
      next: (items) => {
        this.items = items;
      }
    });
  }
  
  toggleItem(item: ShoppingListItem) {
    this.shoppingListService.toggleItemChecked(item.id).subscribe();
    this.telegramService.hapticFeedback('light');
  }
  
  openAddModal() {
    this.showAddModal = true;
    this.newItemName = '';
    this.newItemQuantity = 0;
    this.newItemUnit = 'GRAM';
  }
  
  closeAddModal() {
    this.showAddModal = false;
  }
  
  addItem() {
    if (!this.newItemName.trim()) return;
    
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.addManualItem({
      ingredientName: this.newItemName,
      totalQuantity: this.newItemQuantity,
      unit: this.newItemUnit
    }, weekStartStr).subscribe({
      next: () => {
        this.closeAddModal();
        this.telegramService.hapticFeedback('medium');
      }
    });
  }
  
  regenerateList() {
    const weekStartStr = this.weekStart.toISOString().split('T')[0];
    this.shoppingListService.regenerateShoppingList(weekStartStr).subscribe();
  }
  
  getWeekRange(): string {
    const end = new Date(this.weekStart);
    end.setDate(this.weekStart.getDate() + 6);
    const startStr = this.weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const endStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    return `${startStr} — ${endStr}`;
  }
}
```

- [ ] **Шаг 2: Создать shopping-list.component.html**

```html
<div class="container">
  <div class="header">
    <h1 class="title">Список покупок</h1>
    <button class="icon-btn" (click)="regenerateList()">🔄</button>
  </div>
  <div class="week-range">{{ getWeekRange() }}</div>
  
  <div class="items-list">
    @for (item of items; track item.id) {
      <div class="item" [class.checked]="item.checked" (click)="toggleItem(item)">
        <div class="checkbox">{{ item.checked ? '☑' : '☐' }}</div>
        <div class="item-info">
          <div class="item-name">{{ item.ingredientName }}</div>
          <div class="item-quantity">{{ item.totalQuantity }} {{ item.unit === 'GRAM' ? 'г' : item.unit === 'MILLILITER' ? 'мл' : 'шт' }}</div>
        </div>
        @if (item.manual) {
          <div class="manual-badge">вручную</div>
        }
      </div>
    }
    
    @if (items.length === 0) {
      <div class="empty-state">
        <p>Список пуст</p>
        <p class="subtitle">Сгенерируйте из плана или добавьте вручную</p>
      </div>
    }
  </div>
  
  <button class="fab" (click)="openAddModal()">+</button>
</div>

<app-modal [isOpen]="showAddModal" title="Добавить продукт" (close)="closeAddModal()">
  <div class="modal-form">
    <div class="form-group">
      <label class="form-label">Название</label>
      <input type="text" class="form-input" [(ngModel)]="newItemName" placeholder="Например: Молоко" />
    </div>
    
    <div class="form-row">
      <div class="form-group">
        <label class="form-label">Количество</label>
        <input type="number" class="form-input" [(ngModel)]="newItemQuantity" placeholder="0" />
      </div>
      
      <div class="form-group">
        <label class="form-label">Единица</label>
        <select class="form-select" [(ngModel)]="newItemUnit">
          @for (unit of units; track unit.value) {
            <option [value]="unit.value">{{ unit.label }}</option>
          }
        </select>
      </div>
    </div>
    
    <button class="btn-primary" (click)="addItem()">Добавить</button>
  </div>
</app-modal>
```

- [ ] **Шаг 3: Создать shopping-list.component.scss**

```scss
.container {
  padding: 16px;
  padding-bottom: 80px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: var(--tg-theme-text-color, #000);
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.week-range {
  font-size: 14px;
  color: var(--tg-theme-hint-color, #888);
  margin-bottom: 16px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 8px;
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.7;
  }
  
  &.checked {
    opacity: 0.5;
    
    .item-name {
      text-decoration: line-through;
    }
  }
}

.checkbox {
  font-size: 24px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
}

.item-quantity {
  font-size: 12px;
  color: var(--tg-theme-hint-color, #888);
}

.manual-badge {
  font-size: 10px;
  padding: 4px 8px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border-radius: 4px;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--tg-theme-hint-color, #888);
  
  p {
    margin: 8px 0;
  }
  
  .subtitle {
    font-size: 14px;
  }
}

.fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  font-size: 32px;
  font-weight: 300;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
}

.form-input {
  padding: 12px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.form-select {
  padding: 12px;
  border: 1px solid var(--tg-theme-hint-color, #ccc);
  border-radius: 8px;
  font-size: 14px;
  background: var(--tg-theme-bg-color, #fff);
  color: var(--tg-theme-text-color, #000);
}

.btn-primary {
  padding: 12px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/shopping-list/
git commit -m "feat: add ShoppingList component with checkboxes and manual item addition"
```

---

## Фаза 7: Frontend — Главная и профиль

### Задача 7.1: Создать компонент Home

**Файлы:**
- Создать: `frontend/src/app/features/home/home.component.ts`
- Создать: `frontend/src/app/features/home/home.component.html`
- Создать: `frontend/src/app/features/home/home.component.scss`

- [ ] **Шаг 1: Создать home.component.ts**

```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  sections = [
    { icon: '📖', title: 'Рецепты', route: '/recipes' },
    { icon: '📅', title: 'План на неделю', route: '/plan' },
    { icon: '🛒', title: 'Список покупок', route: '/shopping' },
    { icon: '👤', title: 'Профиль пары', route: '/couple' }
  ];
  
  constructor(private router: Router) {}
  
  navigateTo(route: string) {
    this.router.navigate([route]);
  }
}
```

- [ ] **Шаг 2: Создать home.component.html**

```html
<div class="container">
  <h1 class="title">Планировщик питания</h1>
  
  <div class="sections-grid">
    @for (section of sections; track section.route) {
      <div class="section-card" (click)="navigateTo(section.route)">
        <div class="section-icon">{{ section.icon }}</div>
        <div class="section-title">{{ section.title }}</div>
      </div>
    }
  </div>
</div>
```

- [ ] **Шаг 3: Создать home.component.scss**

```scss
.container {
  padding: 24px;
}

.title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 32px;
  text-align: center;
  color: var(--tg-theme-text-color, #000);
}

.sections-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.section-card {
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 16px;
  padding: 32px 16px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.98);
  }
}

.section-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/home/
git commit -m "feat: add Home component with navigation cards"
```

---

### Задача 7.2: Создать компонент CoupleProfile

**Файлы:**
- Создать: `frontend/src/app/features/couple/couple-profile/couple-profile.component.ts`
- Создать: `frontend/src/app/features/couple/couple-profile/couple-profile.component.html`
- Создать: `frontend/src/app/features/couple/couple-profile/couple-profile.component.scss`

- [ ] **Шаг 1: Создать couple-profile.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { Couple } from '../../../shared/models';

@Component({
  selector: 'app-couple-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './couple-profile.component.html',
  styleUrl: './couple-profile.component.scss'
})
export class CoupleProfileComponent implements OnInit {
  couple: Couple | null = null;
  
  constructor(
    private router: Router,
    private coupleService: CoupleService,
    private telegramService: TelegramService
  ) {}
  
  ngOnInit() {
    this.coupleService.getCouple().subscribe({
      next: (couple) => {
        this.couple = couple;
      }
    });
    
    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }
  
  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }
  
  shareCode() {
    if (this.couple) {
      const url = `https://t.me/your_bot?start=${this.couple.inviteCode}`;
      this.telegramService.share(url, 'Присоединяйся к планировщику питания!');
    }
  }
  
  leaveCouple() {
    if (confirm('Покинуть пару? Все данные будут удалены.')) {
      this.coupleService.leaveCouple().subscribe({
        next: () => {
          this.router.navigate(['/join']);
        }
      });
    }
  }
}
```

- [ ] **Шаг 2: Создать couple-profile.component.html**

```html
<div class="container">
  <h1 class="title">Профиль пары</h1>
  
  @if (couple) {
    <div class="section">
      <h2 class="section-title">Участники</h2>
      <div class="users-list">
        @for (user of couple.users; track user.id) {
          <div class="user-item">
            <div class="user-icon">👤</div>
            <div class="user-name">{{ user.username }}</div>
          </div>
        }
      </div>
    </div>
    
    <div class="section">
      <h2 class="section-title">Код приглашения</h2>
      <div class="invite-code">{{ couple.inviteCode }}</div>
      <button class="btn-share" (click)="shareCode()">Поделиться</button>
    </div>
    
    <button class="btn-leave" (click)="leaveCouple()">Покинуть пару</button>
  }
</div>
```

- [ ] **Шаг 3: Создать couple-profile.component.scss**

```scss
.container {
  padding: 24px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
  color: var(--tg-theme-text-color, #000);
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--tg-theme-text-color, #000);
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 8px;
}

.user-icon {
  font-size: 24px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--tg-theme-text-color, #000);
}

.invite-code {
  font-size: 36px;
  font-weight: bold;
  letter-spacing: 8px;
  text-align: center;
  padding: 24px;
  background: var(--tg-theme-secondary-bg-color, #f0f0f0);
  border-radius: 12px;
  margin-bottom: 16px;
  color: var(--tg-theme-text-color, #000);
}

.btn-share {
  width: 100%;
  padding: 12px;
  background: var(--tg-theme-button-color, #4fc3f7);
  color: var(--tg-theme-button-text-color, #000);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-leave {
  width: 100%;
  padding: 12px;
  background: #ff5252;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
```

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/app/features/couple/couple-profile/
git commit -m "feat: add CoupleProfile component with invite code and leave functionality"
```

---

## Фаза 8: Тестирование и полировка

### Задача 8.1: Обновить app.component

**Файлы:**
- Изменить: `frontend/src/app/app.component.ts`
- Изменить: `frontend/src/app/app.component.html`

- [ ] **Шаг 1: Обновить app.component.ts**

```typescript
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {}
```

- [ ] **Шаг 2: Обновить app.component.html**

```html
<router-outlet />
```

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/app/app.component.ts frontend/src/app/app.component.html
git commit -m "feat: update AppComponent to use router-outlet"
```

---

### Задача 8.2: Запустить backend и frontend

- [ ] **Шаг 1: Запустить backend**

```bash
cd backend
./gradlew bootRun
```

Ожидаемый результат: Backend запускается на порту 8080.

- [ ] **Шаг 2: Запустить frontend**

```bash
cd frontend
npm start
```

Ожидаемый результат: Frontend запускается на порту 4200.

- [ ] **Шаг 3: Проверить приложение**

Открыть браузер и перейти на `http://localhost:4200`. Проверить:
- Экран присоединения к паре работает
- Создание пары генерирует код
- Присоединение по коду работает
- Главная страница отображает карточки
- Навигация между разделами работает

---

### Задача 8.3: Настроить прокси для API

**Файлы:**
- Создать: `frontend/proxy.conf.json`

- [ ] **Шаг 1: Создать proxy.conf.json**

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

- [ ] **Шаг 2: Обновить angular.json**

В `frontend/angular.json` найти секцию `serve` и добавить `proxyConfig`:

```json
"serve": {
  "builder": "@angular-devkit/build-angular:dev-server",
  "configurations": {
    "production": {
      "buildTarget": "frontend:build:production"
    },
    "development": {
      "buildTarget": "frontend:build:development"
    }
  },
  "defaultConfiguration": "development",
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}
```

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/proxy.conf.json frontend/angular.json
git commit -m "feat: configure proxy for API requests in development"
```

---

### Задача 8.4: Написать README

**Файлы:**
- Создать: `README.md`

- [ ] **Шаг 1: Создать README.md**

```markdown
# Telegram Mini App — Планировщик питания для пар

Приложение для пар, позволяющее совместно управлять рецептами, планировать питание на неделю и формировать список покупок.

## Возможности

- **Рецепты**: Сохраняйте рецепты с фото, ингредиентами и инструкциями
- **План на неделю**: Планируйте завтраки, обеды, полдники и ужины на 7 дней
- **Список покупок**: Автоматическая генерация из плана + ручное добавление
- **Совместное использование**: Два пользователя делят общие данные через код-приглашение

## Технологический стек

### Backend
- Java 21
- Spring Boot 3.3.5
- PostgreSQL 15 / H2 (dev)
- Gradle

### Frontend
- Angular 18.2
- TypeScript 5.5
- SCSS
- @twa-dev/sdk (Telegram Web App SDK)

## Запуск

### Backend

```bash
cd backend
./gradlew bootRun
```

Backend запустится на `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

Frontend запустится на `http://localhost:4200`.

## Использование

1. Откройте приложение в Telegram (настройте бота через BotFather)
2. Создайте пару или введите код-приглашение от партнёра
3. Добавляйте рецепты с фото
4. Планируйте питание на неделю
5. Генерируйте список покупок из плана

## Структура проекта

```
shop-telegram-mini/
├── backend/          # Spring Boot API
├── frontend/         # Angular приложение
└── docs/            # Документация и планы
```

## Лицензия

MIT
```

- [ ] **Шаг 2: Коммит**

```bash
git add README.md
git commit -m "docs: add comprehensive README with setup instructions"
```

---

### Задача 8.5: Финальная проверка

- [ ] **Шаг 1: Запустить все тесты backend**

```bash
cd backend
./gradlew test
```

Ожидаемый результат: Все тесты проходят.

- [ ] **Шаг 2: Собрать production build frontend**

```bash
cd frontend
npm run build
```

Ожидаемый результат: Build завершается успешно, файлы создаются в `dist/frontend/browser/`.

- [ ] **Шаг 3: Проверить все функции**

Протестировать полный пользовательский сценарий:
1. Создание пары
2. Присоединение второго пользователя
3. Создание рецепта с фото
4. Добавление рецепта в план на несколько дней
5. Генерация списка покупок
6. Добавление ручного товара
7. Отметка товаров как купленных
8. Просмотр профиля пары
9. Выход из пары

- [ ] **Шаг 4: Финальный коммит**

```bash
git add .
git commit -m "chore: final polish and testing"
```

---

## Итоговый чеклист

- [ ] Backend: Модель данных для пар (User, Couple)
- [ ] Backend: API endpoints для Couple
- [ ] Backend: Загрузка фото через Telegram Bot API
- [ ] Backend: Фильтрация данных по couple_id
- [ ] Backend: Обработка ошибок
- [ ] Frontend: Базовая структура (роутинг, сервисы, guards)
- [ ] Frontend: Экран присоединения к паре
- [ ] Frontend: Рецепты (список, детали, форма)
- [ ] Frontend: План на неделю
- [ ] Frontend: Список покупок
- [ ] Frontend: Главная страница
- [ ] Frontend: Профиль пары
- [ ] Интеграция с Telegram Web App SDK
- [ ] Тестирование всех функций
- [ ] Документация

---

**План завершён!**

План сохранён в `docs/superpowers/plans/2026-06-01-telegram-mini-app-implementation.md`.

**Варианты выполнения:**

**1. Subagent-Driven (рекомендуется)** - Я запускаю отдельный subagent для каждой задачи, проверяю между задачами, быстрая итерация

**2. Inline Execution** - Выполняю задачи в этой сессии с контрольными точками для проверки

Какой подход выбираете?