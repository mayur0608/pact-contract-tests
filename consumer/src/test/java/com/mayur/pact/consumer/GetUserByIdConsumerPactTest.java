package com.mayur.pact.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.mayur.pact.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Consumer Pact test for GET /api/users/{id}
 *
 * This test:
 * 1. Defines what the consumer EXPECTS from the provider
 * 2. Generates a pact file in /pacts directory
 * 3. Verifies our UserServiceClient handles the response correctly
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "UserService")
class GetUserByIdConsumerPactTest {

    // ── Happy path: existing user ────────────────────────────────────────────

    @Pact(consumer = "OrderService", provider = "UserService")
    public V4Pact getUserByIdPact(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("user with id 1 exists")
                .uponReceiving("a request to get user by id 1")
                    .path("/api/users/1")
                    .method("GET")
                    .headers("Accept", "application/json")
                .willRespondWith()
                    .status(200)
                    .headers(java.util.Map.of("Content-Type", "application/json"))
                    .body(newJsonBody(body -> {
                        body.integerType("id", 1);
                        body.stringType("name", "Mayur Sharma");
                        body.stringMatcher("email", "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}", "mayur@example.com");
                        body.stringType("role", "QA_ENGINEER");
                    }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getUserByIdPact")
    void testGetUserById_returnsCorrectUser(MockServer mockServer) throws IOException, InterruptedException {
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());

        User user = client.getUserById(1);

        assertNotNull(user);
        assertEquals(1, user.getId());
        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertTrue(user.getEmail().contains("@"), "Email should be valid format");
        assertNotNull(user.getRole());
    }

    // ── Sad path: user not found ─────────────────────────────────────────────

    @Pact(consumer = "OrderService", provider = "UserService")
    public V4Pact getUserNotFoundPact(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("user with id 999 does not exist")
                .uponReceiving("a request to get a non-existent user")
                    .path("/api/users/999")
                    .method("GET")
                    .headers("Accept", "application/json")
                .willRespondWith()
                    .status(404)
                    .headers(java.util.Map.of("Content-Type", "application/json"))
                    .body(newJsonBody(body -> {
                        body.integerType("status", 404);
                        body.stringType("error", "Not Found");
                        body.stringType("message", "User not found with id: 999");
                    }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getUserNotFoundPact")
    void testGetUserById_throwsExceptionWhenNotFound(MockServer mockServer) {
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> client.getUserById(999));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
