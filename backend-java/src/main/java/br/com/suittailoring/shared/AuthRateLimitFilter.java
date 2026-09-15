package br.com.suittailoring.shared;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Single-instance abuse protection. Edge rate limiting is required for multiple API instances. */
@Component
@ConditionalOnWebApplication
public class AuthRateLimitFilter extends OncePerRequestFilter {
  private record Window(long minute, int count) {}

  private final Map<String, Window> windows = new HashMap<>();

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equals(request.getMethod())
        || !Set.of("/api/auth/login", "/api/admin/auth/login", "/api/auth/register", "/api/cart")
            .contains(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!allow(
        request.getRemoteAddr() + request.getRequestURI(), Instant.now().getEpochSecond() / 60)) {
      response.setHeader("Retry-After", "60");
      response.sendError(429);
      return;
    }
    chain.doFilter(request, response);
  }

  synchronized boolean allow(String key, long minute) {
    windows.entrySet().removeIf(e -> e.getValue().minute() != minute);
    Window previous = windows.get(key);
    if (previous == null && windows.size() >= 10000) return false;
    int count = previous == null ? 0 : previous.count();
    if (count >= 20) return false;
    windows.put(key, new Window(minute, count + 1));
    return true;
  }
}
