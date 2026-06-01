package com.example.backend.telegram;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramInitDataFilter implements Filter {

    @Value("${telegram.bot-token:}")
    private String botToken;

    private final Environment environment;

    public TelegramInitDataFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip validation in dev profile for local testing
        if (isDevProfileActive()) {
            chain.doFilter(request, response);
            return;
        }

        String initData = httpRequest.getHeader("X-Telegram-Init-Data");
        if (initData == null || initData.isBlank()) {
            initData = httpRequest.getParameter("initData");
        }

        if (initData == null || initData.isBlank()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing Telegram init data");
            return;
        }

        if (!validateInitData(initData)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Invalid Telegram init data");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isDevProfileActive() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private boolean validateInitData(String initData) {
        try {
            Map<String, String> params = parseInitData(initData);
            String receivedHash = params.remove("hash");
            if (receivedHash == null || botToken == null || botToken.isBlank()) {
                return false;
            }

            List<String> dataCheckStrings = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.toList());

            String dataCheckString = String.join("\n", dataCheckStrings);

            String secretKey = hmacSha256("WebAppData", botToken);
            String computedHash = hmacSha256Hex(secretKey, dataCheckString);

            return computedHash.equalsIgnoreCase(receivedHash);
        } catch (Exception e) {
            log.error("Telegram init data validation error", e);
            return false;
        }
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }
        return result;
    }

    private String hmacSha256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return new String(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    private String hmacSha256Hex(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
