# Pact Contract Tests

![CI](https://github.com/mayur0608/pact-contract-tests/actions/workflows/pact-ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk)
![Pact](https://img.shields.io/badge/Pact-4.6.7-E4393C?style=flat-square)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-C71A36?style=flat-square&logo=apachemaven)

Consumer-driven contract testing framework using **Pact JVM + JUnit 5**, demonstrating the full contract testing lifecycle between a consumer (OrderService) and a provider (UserService).

---

## What is Contract Testing?

Contract testing ensures two services can communicate correctly **without running both at the same time**. The consumer defines what it expects (a "pact"), and the provider verifies it can fulfil those expectations.

```
Consumer (OrderService)          Provider (UserService)
        │                                │
        │  1. Write pact test            │
        │  2. Generate pact file ──────► │
        │                                │  3. Verify pact
        │  ◄──────────────── Pass/Fail   │
```

---

## Project Structure

```
pact-contract-tests/
├── consumer/                          # OrderService — defines expectations
│   └── src/test/java/
│       ├── GetUserByIdConsumerPactTest.java   # GET /api/users/{id} contracts
│       └── UserCollectionConsumerPactTest.java # GET /api/users + POST contracts
│
├── provider/                          # UserService — Spring Boot REST API
│   └── src/
│       ├── main/java/                 # Controller, Service, Model
│       └── test/java/
│           └── UserServiceProviderPactTest.java # Provider verification
│
├── pacts/                             # Generated pact JSON files (git-ignored)
└── pom.xml                            # Multi-module Maven parent
```

---

## Contracts Covered

| Consumer | Provider | Interaction | Status |
|---|---|---|---|
| OrderService | UserService | `GET /api/users/1` — user exists | ✅ |
| OrderService | UserService | `GET /api/users/999` — user not found (404) | ✅ |
| OrderService | UserService | `GET /api/users` — list all users | ✅ |
| OrderService | UserService | `POST /api/users` — create user | ✅ |

---

## Running the Tests

### Prerequisites
- Java 17+
- Maven 3.8+

### Step 1 — Run consumer tests (generates pact files)
```bash
mvn test -pl consumer
```
Pact JSON files are written to `/pacts`.

### Step 2 — Verify provider against pacts
```bash
mvn test -pl provider
```

### Run everything
```bash
mvn test
```

---

## Key Concepts Demonstrated

**PactDslJsonBody matchers** — type-safe, flexible assertions:
```java
body.integerType("id", 1);                          // any integer
body.stringType("name", "Mayur");                   // any string
body.stringMatcher("email", "[a-z]+@[a-z]+\\.com"); // regex
```

**Provider states** — set up data before each interaction:
```java
@State("user with id 1 exists")
void userWithId1Exists() {
    Mockito.when(userService.findById(1)).thenReturn(Optional.of(user));
}
```

**Consumer independence** — consumer tests run against a Pact mock server, no provider needed.

---

## CI Pipeline

GitHub Actions runs consumer → provider in sequence on every push, pull request, and daily health-check schedule:

```
push / pull request / schedule → consumer tests → pact files → provider verification → pass/fail
```

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Pact JVM 4.6.7 | Contract definition and verification |
| JUnit 5 | Test runner |
| Spring Boot 3 | Provider REST API |
| MockMvc | Provider-side mock HTTP layer |
| Mockito | Provider state setup |
| Maven | Multi-module build |
| GitHub Actions | CI pipeline |
