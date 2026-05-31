package com.mayur.pact.service;

import com.mayur.pact.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserService {

    private final Map<Integer, User> store = new HashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public UserService() {
        store.put(1, User.builder().id(1).name("Mayur Sharma").email("mayur@example.com").role("QA_ENGINEER").build());
        store.put(2, User.builder().id(2).name("Jane Doe").email("jane@example.com").role("TESTER").build());
        idSequence.set(3);
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public User create(User user) {
        int newId = idSequence.getAndIncrement();
        User saved = User.builder()
                .id(newId)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        store.put(newId, saved);
        return saved;
    }
}
