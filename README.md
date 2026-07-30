# ING Mortgage Payment Tracker Service (MVP)

An production-ready MVP backend system for tracking mortgage principal reductions and simulating upcoming multi-month amortization projections.

## 🚀 Tech Stack
- Java 17
- Spring Boot 3
- Spring Data JPA
- H2 In-Memory Database
- Springdoc OpenAPI (Swagger UI)

## 🛠️ How To Run Locally

### Option A: Standard Maven Run
Ensure you have JDK 17+ installed. Run from the project root:
```bash
./mvnw clean spring-boot:run
```


## 🧪 Running Automated Tests
Run Unit and Integration verification test suites:
```bash
./mvnw test
```

## 📖 Live API Documentation (Swagger)
Once the service boots up, access interactive document sandboxes to execute live tracking tasks directly from your browser:
- **Swagger Dashboard**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON Docs**: http://localhost:8080/v3/api-docs

## 📡 API Usage Examples

### 1. Record a Payment
- **HTTP Verb / Path**: `POST /mortgages/1/payments`
- **Payload**:
```json
{
  "paymentDate": "2026-07-30",
  "amount": 300.00
}
```
- **Response (200 OK)**:
```json
{
  "remainingPrincipal": 9500.00
}
```

### 2. View Payment Breakdown
- **HTTP Verb / Path**: `GET /mortgages/1/payments`
- **Response (200 OK)**:
```json
[
  {
    "paymentDate": "2026-07-15",
    "totalPayment": 300.00,
    "principalPaid": 250.00,
    "interestPaid": 50.00,
    "remainingPrincipal": 9750.00
  }
]
```

### 3. Project Future Balances
- **HTTP Verb / Path**: `GET /mortgages/1/projection?months=2`
- **Response (200 OK)**:
```json
[
  {
    "month": "2026-08",
    "totalPayment": 300.00,
    "principalPaid": 251.25,
    "interestPaid": 48.75,
    "remainingPrincipal": 9498.75
  }
]
```
