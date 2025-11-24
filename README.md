# Bitespeed Identity Reconciliation – Backend Service

This repository contains a production-ready Spring Boot implementation of the **Identity Reconciliation** problem from Bitespeed. It exposes a single endpoint `/identify` that merges customer contact records based on shared email or phone number, and returns a unified identity profile.

---

## 🚀 Live API Endpoint

The service is deployed on **Render** and available here:

**POST**
`https://bitespeed-identity-mwhm.onrender.com/identify`

---

## 📌 Problem Summary

A user may provide:

* `email`
* `phoneNumber`
* or both

But multiple customer entries may represent the *same person* across time. Your system must:

### ✔ Identify all related contacts (via email or phone)

### ✔ Decide which one is the **primary** record

### ✔ Convert the rest to **secondary**

### ✔ Maintain proper linked relationships

### ✔ Return a merged identity profile

This service ensures consistent identity resolution across scattered customer data.

---

## 🏗 Tech Stack

* **Java 17+ / Java 23 tested**
* **Spring Boot 3**
* **Spring Data JPA + Hibernate**
* **PostgreSQL** (production on Render)
* **Docker (for deployment on Render)**
* **Maven**

---

## 📡 API Specification

### **POST /identify**

#### Request Body

```json
{
  "email": "test@example.com",
  "phoneNumber": "1234567890"
}
```

Both fields are optional — at least one must be present.

---

## 📤 Sample Response

```json
{
  "contact": {
    "primaryContactId": 2,
    "emails": ["test@example.com", "alias@example.com"],
    "phoneNumbers": ["1234567890"],
    "secondaryContactIds": [3]
  }
}
```

### Response Fields:

| Field                 | Description                              |
| --------------------- | ---------------------------------------- |
| `primaryContactId`    | The canonical "main" contact             |
| `emails`              | All emails linked to the identity        |
| `phoneNumbers`        | All phone numbers linked to the identity |
| `secondaryContactIds` | IDs of merged secondary contacts         |

---

## 🧠 How Identity Resolution Works

1. The service checks if the provided email or phone number already exists.
2. If no match exists → a **new primary contact** is created.
3. If matches exist:

   * Find the earliest primary → that becomes the **main primary**
   * Convert later primaries → **secondary**
   * If a new email/phone is submitted → create a **secondary** linked to the main primary
4. Return a unified identity record.

---

## 🗄 Database Schema

| Field            | Type                     | Description                            |
| ---------------- | ------------------------ | -------------------------------------- |
| `id`             | BIGINT                   | Primary key                            |
| `phoneNumber`    | VARCHAR                  | Optional                               |
| `email`          | VARCHAR                  | Optional                               |
| `linkedId`       | BIGINT                   | Points to primary contact if secondary |
| `linkPrecedence` | `primary` or `secondary` |                                        |
| `createdAt`      | TIMESTAMP                | Auto-managed                           |
| `updatedAt`      | TIMESTAMP                | Auto-managed                           |
| `deletedAt`      | TIMESTAMP                | Nullable                               |

---

## 🐳 Deployment (Render + Docker)

This project is deployed on **Render** using Docker.

### 1. Create a PostgreSQL database on Render

Copy the **Internal DB URL** (Render dashboard) — it will look like:

```
postgresql://USER:PASSWORD@HOST:PORT/DBNAME
```

### 2. Add Environment Variables in Render

| Key                 | Value                                    |
| ------------------- | ---------------------------------------- |
| `DATABASE_URL`      | `jdbc:postgresql://<HOST>:5432/<DBNAME>` |
| `DATABASE_USERNAME` | `<user>`                                 |
| `DATABASE_PASSWORD` | `<password>`                             |
| `PORT`              | `8080`                                   |

> Note: Render provides an internal URL; convert it to JDBC format (prefix with `jdbc:postgresql://` and include the host and port). Provide credentials separately.

### 3. `application.properties` (production)

```properties
server.port=${PORT:8080}

spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

logging.level.org.hibernate.SQL=debug
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=trace
```

### 4. Deploy using Render’s Docker setup

Render will build your Dockerfile, run the container, and expose the public URL.

---

## 🧪 Testing

### Create a new customer

```bash
curl -X POST "https://bitespeed-identity-mwhm.onrender.com/identify" \
-H "Content-Type: application/json" \
-d '{"email":"unique1@test.com", "phoneNumber":"9000012345"}'
```

### Merge with related contact

```bash
curl -X POST "https://bitespeed-identity-mwhm.onrender.com/identify" \
-H "Content-Type: application/json" \
-d '{"email":"alias@test.com", "phoneNumber":"9000012345"}'
```

---

## 📁 Project Structure

```
src/
 ├── controller/
 │     └── ContactController.java
 ├── service/
 │     └── ContactService.java
 ├── model/
 │     ├── Contact.java
 │     └── LinkPrecedence.java
 ├── repository/
 │     └── ContactRepository.java
 └── BitespeedIdentityApplication.java
Dockerfile
pom.xml
README.md
```

---

## ⭐ Author

Built by **Yashas**

---

## ✔ Status: Fully Functional & Deployed

You can test the live API at:
**[https://bitespeed-identity-mwhm.onrender.com/identify](https://bitespeed-identity-mwhm.onrender.com/identify)**
