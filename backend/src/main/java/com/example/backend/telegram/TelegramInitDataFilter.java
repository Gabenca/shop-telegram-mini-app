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
