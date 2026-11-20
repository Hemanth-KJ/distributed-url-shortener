# Distributed URL Shortener — Backend

A scalable, distributed URL shortening service built using **Java, Spring Boot, PostgreSQL, Redis, Apache Kafka, Docker, and Nginx**.

This project demonstrates backend engineering concepts including distributed system architecture, caching, asynchronous event processing, JWT authentication, URL expiration, click analytics, and cloud deployment.

## Live Application

- **Live Website:** http://136.64.82.6/
- **GitHub Repository:** https://github.com/Hemanth-KJ/distributed-url-shortener

> The application is hosted on a Google Cloud VM. Availability depends on the VM being operational.

## Key Features

- **URL Shortening:** Generate unique short codes for long URLs.
- **Fast Redirection:** Redirect shortened links to their original destinations.
- **JWT Authentication:** Secure user registration, login, and protected API access.
- **Redis Caching:** Cache URL mappings to reduce repeated database queries.
- **Kafka Event Processing:** Decouple click analytics processing from URL redirection.
- **PostgreSQL Persistence:** Store users, shortened URLs, and associated metadata.
- **Click Analytics:** Track URL clicks and provide analytics information.
- **URL Expiration:** Support time-limited shortened links.
- **QR Code Generation:** Generate QR codes for shortened URLs.
- **User URL Management:** Retrieve URLs belonging to authenticated users.
- **Docker Deployment:** Run backend infrastructure using Docker Compose.
- **Nginx Reverse Proxy:** Route API requests and serve the frontend.
- **Health Monitoring:** Monitor application health with Spring Boot Actuator.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend development |
| Spring Boot | REST API framework |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| PostgreSQL | Relational database |
| Redis | Distributed caching |
| Apache Kafka | Asynchronous event processing |
| Maven | Dependency management |
| Docker | Containerization |
| Docker Compose | Service orchestration |
| Nginx | Reverse proxy |
| Google Cloud Compute Engine | Cloud deployment |

## System Architecture

```text
                     Client Browser
                           |
                           v
                    Nginx :80
                      /       \
                     /         \
              React Frontend   /api/v1
                                  |
                                  v
                         Spring Boot API
                                  |
                 +----------------+----------------+
                 |                |                |
                 v                v                v
             Redis Cache      PostgreSQL      Apache Kafka
                 |              Database        Events
                 |                |                |
                 +----------------+----------------+
```

## URL Shortening Workflow

1. A user logs in and receives a JWT authentication token.
2. The user submits a long URL through the frontend.
3. Spring Boot validates the request and generates a unique short code.
4. The URL mapping and associated metadata are stored in PostgreSQL.
5. The shortened URL is returned to the user.
6. When the shortened URL is accessed, the backend looks up its destination.
7. Redis caching helps reduce repeated PostgreSQL lookups.
8. The application checks URL expiration before allowing redirection.
9. Click events can be processed asynchronously through Kafka.
10. Analytics information becomes available through the API.

## Project Structure

```text
distributed-url-shortener/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hemanth/
│   │   │           └── distributedurlshortener/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── nginx/
│   └── nginx.conf
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites

Install the following:

- Git
- Java (version compatible with `pom.xml`)
- Maven or Maven Wrapper
- Docker
- Docker Compose

### Clone Repository

```bash
git clone https://github.com/Hemanth-KJ/distributed-url-shortener.git

cd distributed-url-shortener
```

### Environment Configuration

Configure your application's database, Redis, Kafka, and authentication settings.

Example environment variables:

```env
SPRING_PROFILES_ACTIVE=prod

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/distributed_url_shortener
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_database_password

SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

JWT_SECRET=replace_with_a_secure_random_secret
```

These are example values. Match them to the actual properties and environment-variable mappings in your Spring Boot application.

Never commit real passwords or JWT secrets to GitHub.

### Build the Application

Using Maven Wrapper:

```bash
./mvnw clean package -DskipTests
```

On Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### Run Using Docker Compose

Build and start all services:

```bash
docker compose up -d --build
```

Check running containers:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f app
```

Check backend health from inside the application container:

```bash
docker compose exec app \
  curl -f http://localhost:8080/actuator/health
```

If `curl` is unavailable in the container, use an available HTTP client or test the endpoint from a network location where port 8080 is accessible.

### Stop the Application

```bash
docker compose stop
```

### Restart the Application

```bash
docker compose start
```

### Rebuild After Code Changes

```bash
docker compose up -d --build
```

**Important:** Avoid `docker compose down -v` unless you intentionally want to delete named volumes. Removing the PostgreSQL volume can permanently delete database data.

## REST API Documentation

Base URL:

```text
http://136.64.82.6/api/v1
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate user |
| POST | `/urls` | Create a shortened URL |
| GET | `/users/me/urls` | Retrieve authenticated user's URLs |
| GET | `/urls/{shortCode}/analytics` | Retrieve click analytics |
| GET | `/urls/{shortCode}/qrcode` | Generate a QR code, if enabled |

The short-link redirection route is served separately from the API routes, according to the backend and Nginx configuration.

### Authentication Example

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Example request:

```json
{
  "email": "user@example.com",
  "password": "your_password"
}
```

A successful login returns a JWT authentication token.

Use the token for protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

### URL Analytics Example

```http
GET /api/v1/urls/google/analytics
Authorization: Bearer <JWT_TOKEN>
```

Example analytics data:

```json
{
  "originalUrl": "https://www.google.com",
  "shortCode": "google",
  "clickCount": 2,
  "createdAt": "2026-07-29T15:35:53.752876",
  "expiresAt": "2026-07-29T15:40:00"
}
```

## Redis Caching

Redis provides a fast lookup layer for frequently accessed shortened URLs.

Benefits include:

- Reduced repeated database lookups
- Lower database load
- Faster access to cached URL mappings
- Improved scalability under repeated requests

## Apache Kafka Integration

Kafka is used to support asynchronous event processing.

Instead of coupling every analytics operation to the main redirection request, events can be processed separately.

This architecture supports better separation of responsibilities between URL redirection and analytics.

## PostgreSQL Database

PostgreSQL stores persistent application data, including:

- User account information
- Shortened URL mappings
- Original URLs
- Short codes
- URL creation timestamps
- Expiration timestamps
- Click-related metadata

## Cloud Deployment

The application is deployed using Google Cloud Compute Engine.

**Production URL:** http://136.64.82.6/

The deployment uses:

1. Docker Compose to manage application services.
2. Nginx to serve the React frontend.
3. Nginx reverse proxy rules for `/api/` requests.
4. Spring Boot for backend business logic.
5. PostgreSQL for persistent data.
6. Redis for caching.
7. Kafka for asynchronous event processing.

### Deployment Architecture

```text
              Google Cloud VM
                    |
                Port 80
                    |
                  Nginx
                 /     \
                /       \
         React Build    /api/
                          |
                          v
                     Spring Boot
                          |
                +---------+---------+
                |         |         |
             Postgres    Redis     Kafka
```

## Security Considerations

- JWT authentication for protected APIs
- Password hashing through the authentication layer
- Environment-based credential configuration
- Backend authorization checks
- Input validation
- Expiration enforcement for time-limited URLs

The current public address uses HTTP. HTTPS, rate limiting, secret management, and additional security hardening are recommended before handling sensitive production traffic.

## Future Enhancements

- HTTPS with a custom domain
- CI/CD with GitHub Actions
- Distributed rate limiting
- Load balancing across multiple Spring Boot instances
- Automated integration and performance tests
- Centralized logging and monitoring
- Advanced geographical analytics
- Custom branded short domains
- Improved expiration handling and cache invalidation

## Author

**Hemanth K J**

GitHub: https://github.com/Hemanth-KJ

## License

Add a `LICENSE` file to specify the terms under which this project can be used and distributed.
