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
import java.util.List;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArrayMinLike;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Consumer Pact tests for:
 *  - GET /api/users  → list all users
 *  - POST /api/users → create a new user
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "UserService")
class UserCollectionConsumerPactTest {

    // ── GET all users ────────────────────────────────────────────────────────

    @Pact(consumer = "OrderService", provider = "UserService")
    public V4Pact getAllUsersPact(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("at least one user exists")
                .uponReceiving("a request to get all users")
                    .path("/api/users")
                    .method("GET")
                    .headers("Accept", "application/json")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(newJsonArrayMinLike(1, array ->
                        array.object(obj -> {
                            obj.integerType("id");
                            obj.stringType("name");
                            obj.stringType("email");
                            obj.stringType("role");
                        })
                    ).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getAllUsersPact")
    void testGetAllUsers_returnsNonEmptyList(MockServer mockServer) throws IOException, InterruptedException {
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());

        List<User> users = client.getAllUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should return at least one user");
        users.forEach(u -> {
            assertNotNull(u.getId());
            assertNotNull(u.getName());
            assertNotNull(u.getEmail());
        });
    }

    // ── POST create user ─────────────────────────────────────────────────────

    @Pact(consumer = "OrderService", provider = "UserService")
    public V4Pact createUserPact(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("the user service is available")
                .uponReceiving("a request to create a new user")
                    .path("/api/users")
                    .method("POST")
                    .headers("Content-Type", "application/json")
                    .body(newJsonBody(body -> {
                        body.stringType("name", "Jane Doe");
                        body.stringType("email", "jane@example.com");
                        body.stringType("role", "TESTER");
                    }).build())
                .willRespondWith()
                    .status(201)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(newJsonBody(body -> {
                        body.integerType("id");
                        body.stringType("name", "Jane Doe");
                        body.stringType("email", "jane@example.com");
                        body.stringType("role", "TESTER");
                    }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createUserPact")
    void testCreateUser_returnsCreatedUserWithId(MockServer mockServer) throws IOException, InterruptedException {
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());

        User newUser = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .role("TESTER")
                .build();

        User created = client.createUser(newUser);

        assertNotNull(created);
        assertNotNull(created.getId(), "Created user must have an ID assigned");
        assertEquals("Jane Doe", created.getName());
        assertEquals("jane@example.com", created.getEmail());
    }
}
