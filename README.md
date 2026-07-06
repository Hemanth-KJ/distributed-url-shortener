# distributed-url-shortener
Distributed URL Shortener using Spring Boot

# Distributed URL Shortener

A production-grade distributed URL Shortener inspired by TinyURL and Bitly, built using Java, Spring Boot, Redis, Kafka, PostgreSQL, Docker, and Kubernetes.

The goal of this project is not only to shorten URLs but also to demonstrate scalable backend architecture, distributed systems concepts, event-driven communication, caching, monitoring, and cloud-native deployment.

---

#  Project Goals

- Generate unique short URLs
- Redirect users with minimal latency
- Support URL expiration
- Track click analytics
- Generate QR codes for shortened URLs
- Implement Redis caching
- Process analytics asynchronously using Kafka
- Provide Admin APIs
- Secure APIs using JWT Authentication
- Deploy using Docker and Kubernetes
- Monitor using Prometheus and Grafana
- Follow production-grade coding standards

---

#  High-Level Architecture

```
                    Client
                       │
                       ▼
                Load Balancer
                       │
        ┌──────────────┴──────────────┐
        ▼                             ▼
   URL Service                  URL Service
        │
        ▼
    Redis Cache
        │
 Cache Hit / Miss
        │
        ▼
   PostgreSQL Database
        │
        ▼
 Publish Click Event
        │
        ▼
      Kafka Topic
        │
        ▼
 Analytics Service
        │
        ▼
 Analytics Database
```

---

#  Core Features

## URL Shortening
- Generate unique short URLs
- Base62 Encoding
- Snowflake ID Generator
- Collision Handling

## URL Redirection
- Fast redirection
- Redis Cache
- Expiring URLs

## Analytics
- Click Count
- Browser
- Device
- Operating System
- Country
- Timestamp
- Referrer

## Security
- JWT Authentication
- Role-Based Authorization

## Admin
- View URLs
- Delete URLs
- Dashboard APIs
- Analytics APIs

---

#  Technology Stack

## Backend
- Java 21+
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Validation

## Database
- PostgreSQL

## Cache
- Redis

## Messaging
- Apache Kafka

## Build Tool
- Maven

## Containerization
- Docker
- Docker Compose

## Orchestration
- Kubernetes

## Monitoring
- Prometheus
- Grafana

## Testing
- JUnit 5
- Mockito
- Testcontainers

---

#  Planned Project Structure

```
distributed-url-shortener/

├── gateway-service/
├── url-service/
├── analytics-service/
├── common-library/
├── docker/
├── kubernetes/
├── monitoring/
├── docs/
├── architecture/
├── scripts/
├── load-tests/
├── README.md
```

---

#  Planned Modules

### URL Service
- Create Short URL
- Redirect URL
- URL Expiration
- QR Code Generation

### Analytics Service
- Click Tracking
- Geo Location
- Device Detection
- Browser Detection
- Reporting

### Gateway
- Authentication
- Authorization
- Rate Limiting
- Request Routing

---

#  Planned Enhancements

- Redis Cache
- Bloom Filter
- Distributed Locking
- Event-Driven Architecture
- Kafka Consumers
- Circuit Breaker
- Retry Mechanism
- Distributed Tracing
- API Rate Limiting
- Load Testing
- Horizontal Scaling

---

#  Learning Objectives

This project is designed to gain practical experience with:

- Core Java
- Spring Boot
- REST APIs
- PostgreSQL
- Redis
- Kafka
- Docker
- Kubernetes
- JWT
- Distributed Systems
- Caching
- Event-Driven Architecture
- Microservices
- Monitoring
- CI/CD
- System Design

---

#  Development Roadmap

- [ ] Project Setup
- [ ] Database Design
- [ ] URL Shortening API
- [ ] Base62 Encoder
- [ ] Snowflake ID Generator
- [ ] URL Redirect API
- [ ] Redis Integration
- [ ] Kafka Integration
- [ ] Analytics Service
- [ ] Bloom Filter
- [ ] Distributed Lock
- [ ] QR Code Generation
- [ ] JWT Authentication
- [ ] Admin APIs
- [ ] Docker
- [ ] Kubernetes
- [ ] Monitoring
- [ ] CI/CD Pipeline
- [ ] Performance Testing

---

#  License

This project is for educational purposes and demonstrates production-grade backend development concepts.
