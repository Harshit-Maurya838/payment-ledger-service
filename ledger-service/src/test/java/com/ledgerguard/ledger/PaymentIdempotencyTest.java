package com.ledgerguard.ledger;

import com.ledgerguard.ledger.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PaymentIdempotencyTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private PaymentRepository paymentRepository;

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private HttpResponse<String> post(String key, UUID merchantId, long amount) throws Exception {
        String body = "{\"merchantId\":\"" + merchantId + "\",\"amount\":" + amount + ",\"currency\":\"INR\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/payments"))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private long paymentCount(UUID merchantId) {
        return paymentRepository.findAll().stream()
                .filter(p -> merchantId.equals(p.getMerchantId()))
                .count();
    }

    @Test
    void sameKeyTwiceReturnsTheSamePayment() throws Exception {
        UUID merchantId = UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        HttpResponse<String> first = post(key, merchantId, 10000);
        HttpResponse<String> second = post(key, merchantId, 10000);

        assertThat(first.statusCode()).isEqualTo(201);
        assertThat(first.body()).contains("CAPTURED");
        assertThat(second.statusCode()).isEqualTo(200);
        assertThat(second.body()).isEqualTo(first.body());
        assertThat(paymentCount(merchantId)).isEqualTo(1);
    }

    @Test
    void sameKeyWithDifferentBodyReturns422() throws Exception {
        UUID merchantId = UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        HttpResponse<String> first = post(key, merchantId, 10000);
        HttpResponse<String> second = post(key, merchantId, 20000);

        assertThat(first.statusCode()).isEqualTo(201);
        assertThat(second.statusCode()).isEqualTo(422);
        assertThat(paymentCount(merchantId)).isEqualTo(1);
    }

    @Test
    void fiftyParallelRequestsWithOneKeyCreateExactlyOnePayment() throws Exception {
        UUID merchantId = UUID.randomUUID();
        String key = UUID.randomUUID().toString();
        int threads = 50;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<HttpResponse<String>>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return post(key, merchantId, 10000);
                }));
            }
            assertThat(ready.await(30, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            List<HttpResponse<String>> responses = new ArrayList<>();
            for (Future<HttpResponse<String>> f : futures) {
                responses.add(f.get(60, TimeUnit.SECONDS));
            }

            List<HttpResponse<String>> created = responses.stream().filter(r -> r.statusCode() == 201).toList();
            assertThat(created).hasSize(1);
            assertThat(responses).allSatisfy(r -> assertThat(r.statusCode()).isIn(200, 201, 409));
            String createdBody = created.get(0).body();
            assertThat(responses).filteredOn(r -> r.statusCode() == 200)
                    .allSatisfy(r -> assertThat(r.body()).isEqualTo(createdBody));
            assertThat(paymentCount(merchantId)).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
