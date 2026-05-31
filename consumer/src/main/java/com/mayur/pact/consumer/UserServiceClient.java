package com.mayur.pact.consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayur.pact.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP client that the consumer uses to call the User Service.
 * Pact tests verify this client's expectations against the provider.
 */
public class UserServiceClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public User getUserById(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new RuntimeException("User not found with id: " + id);
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected status: " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), User.class);
    }

    public List<User> getAllUsers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected status: " + response.statusCode());
        }
        return Arrays.asList(objectMapper.readValue(response.body(), User[].class));
    }

    public User createUser(User user) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create user, status: " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), User.class);
    }
}
