package br.com.suittailoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "app.jobs.enabled=false")
@ActiveProfiles("worker")
class WorkerContextTest {
  @Test
  void workerStartsWithoutHttpServer() {}
}
