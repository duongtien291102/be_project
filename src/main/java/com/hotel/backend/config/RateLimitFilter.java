package com.hotel.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 15;
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final ConcurrentHashMap<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        boolean isRateLimitedEndpoint = "POST".equalsIgnoreCase(method)
                && ("/api/auth/login".equals(uri) || "/api/auth/register".equals(uri));

        if (!isRateLimitedEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        String key = clientIp + ":" + uri;
        long now = System.currentTimeMillis();

        if (requestLogs.size() > MAX_TRACKED_KEYS) {
            requestLogs.clear();
        }

        Deque<Long> timestamps = requestLogs.computeIfAbsent(key, k -> new ArrayDeque<>());
        boolean allowed;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                allowed = false;
            } else {
                timestamps.addLast(now);
                allowed = true;
            }
        }

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Quá nhiều yêu cầu đăng nhập/đăng ký từ địa chỉ này. Vui lòng thử lại sau 1 phút.\",\"data\":null}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
