package com.example.backend.telegram;

import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TelegramInitDataFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(TelegramInitDataFilter.class.getName());

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (requestUri.equals("/actuator/health") || requestUri.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (environment.matchesProfiles("dev") && !environment.matchesProfiles("prod")) {
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

            authenticateUser(request, user);
            filterChain.doFilter(request, response);
            return;
        }

        if (botToken == null || botToken.isBlank()) {
            LOGGER.severe("Telegram bot token is not configured");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Auth configuration error");
            return;
        }

        String initData = request.getHeader("X-Telegram-Init-Data");
        if (initData == null) {
            initData = request.getParameter("initData");
        }

        if (initData == null || initData.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Telegram init data");
            return;
        }

        try {
            if (!validateInitData(initData)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Telegram init data");
                return;
            }

            Map<String, String> params = parseInitData(initData);
            String userJson = params.get("user");

            if (userJson == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing user data");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> userData = objectMapper.readValue(userJson, Map.class);
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

            authenticateUser(request, user);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.severe("Failed to validate Telegram init data: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }

    private void authenticateUser(HttpServletRequest request, User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user.getTelegramId(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        auth.setDetails(user.getId());
        SecurityContextHolder.getContext().setAuthentication(auth);

        request.setAttribute("telegramId", user.getTelegramId());
        request.setAttribute("username", user.getUsername());
        request.setAttribute("userId", user.getId());
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

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
