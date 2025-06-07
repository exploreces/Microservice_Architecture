# 📚 Library Management System - Microservices Architecture

This project is a **Library Management System** built using **Java Spring Boot**, following **Microservices Architecture** principles. It manages books, users, and overall library workflows in a modular, scalable manner.

## 🧰 Tech Stack

- **Backend**: Java 17, Spring Boot
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Resilience**: Circuit Breaker using Resilience4j
- **Database**: MySQL (or any SQL-based system)
- **Architecture**: Microservices

## 🧩 Microservices Overview

- **Eureka Server** – Service registry and discovery.
- **Books Service** – Manages book-related operations.
- **Users Service** – Manages user-related operations.
- **Library Service** – Handles library transactions like borrowing/returning books.
- **API Gateway** – Centralized entry point for all client requests.

## ✅ Prerequisites
Make sure the following tools are installed:

- Java 17+
- Maven
- MySQL installed and running
- IDE (e.g., IntelliJ IDEA, Eclipse)
- Postman or cURL for API testing

## ▶️ Execution Order (Important)
Start services in the following order to ensure correct registration and communication:

1. **Eureka Server**
    - Run `RegistryApplication.java`

2. **Books Service**
    - Run `BooksApplication.java`

3. **Users Service**
    - Run `UsersApplication.java`

4. **Library Service**
    - Run `LibraryApplication.java`

5. **API Gateway**
    - Run `ApiGatewayApplication.java`


## ⚙️ SQL Configuration (`application.properties`)

Each service with a database requires the following configuration in its `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
