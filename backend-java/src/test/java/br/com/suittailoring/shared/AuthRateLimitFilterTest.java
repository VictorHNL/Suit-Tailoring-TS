package br.com.suittailoring.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthRateLimitFilterTest {
  @Test
  void limitsBurstAndResetsNextMinute() {
    var filter = new AuthRateLimitFilter();
    for (int i = 0; i < 20; i++) assertTrue(filter.allow("client", 10));
    assertFalse(filter.allow("client", 10));
    assertTrue(filter.allow("client", 11));
  }
}
