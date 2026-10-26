# URL Shortener

A URL Shortener is a web application that converts long URLs into short, easy-to-share links. When a user opens a shortened URL, the application redirects them to the original URL.

The project is built using Java and Spring Boot, with MySQL for persistent storage. It is designed to demonstrate REST API development, database integration, URL redirection, validation, and automated testing.

## Features

- **Short URL Generation:** Converts long URLs into short links.
- **Custom Aliases:** Allows users to request a custom short code.
- **URL Redirection:** Redirects users to the original URL using the short code.
- **URL Validation:** Validates URLs before saving them.
- **Expiration Support:** Supports optional expiration dates for shortened URLs.
- **Duplicate Alias Handling:** Prevents duplicate short codes.
- **REST API:** Provides endpoints for creating and accessing shortened URLs.
- **Database Integration:** Stores URL information in MySQL.
- **Automated Testing:** Includes tests for service, controller, and repository layers.
- **Redis Caching:** Planned enhancement for improving URL lookup performance.

## Technologies Used

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Maven
- **Database:** MySQL
- **ORM:** Spring Data JPA / Hibernate
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit, Mockito, Spring Boot Test
- **Integration Testing:** Testcontainers
- **Caching (planned):** Redis
- **Frontend (if enabled):** HTML, CSS, JavaScript, Fetch API
- **Development Tools:** IntelliJ IDEA, Docker

## Project Structure

```text
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java
│   │   │   ├── service/
│   │   │   │   └── UrlService.java
│   │   │   ├── repository/
│   │   │   │   └── UrlRepository.java
│   │   │   ├── entity/
│   │   │   │   └── ShortUrl.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateUrlRequest.java
│   │   │   │   └── CreateUrlResponse.java
│   │   │   └── UrlShortenerApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/
│   │           └── js/
│   └── test/
│       └── java/com/urlshortener/
├── pom.xml
└── README.md
```

*Note: The structure above represents the main application files. Include optional files only if they exist in your project.*

## Prerequisites

Install the following before running the application:

- Java version compatible with the project's `pom.xml`
- MySQL Server
- Maven, or the Maven Wrapper included with the project
- IntelliJ IDEA or another Java IDE
- Docker, if you want to run Redis or Testcontainers

## Database Setup

1. Start your MySQL server.
2. Create the database:

```sql
CREATE DATABASE url_shortener_db;
```

3. Configure your database connection in `src/main/resources/application.properties`:

```properties
spring.application.name=url-shortener
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

app.base-url=${APP_BASE_URL:http://localhost:8080}
```

Set `DB_PASSWORD` to your MySQL password in your IDE's environment variables before starting the application.

The database schema must already match the JPA entities when using `ddl-auto=validate`. Do not commit real database passwords or other secrets to GitHub.

## Run the Application

1. Clone the repository:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

2. Open the project in IntelliJ IDEA.
3. Configure the database credentials.
4. Ensure MySQL is running and the database exists.
5. Run `UrlShortenerApplication.java`.

The application starts at:

```text
http://localhost:8080
```

If the frontend is configured, open the URL in your browser to use it.

## API Endpoints

### 1. Create a Short URL

**Endpoint:** `POST /api/urls`

**Content-Type:** `application/json`

Example request:

```json
{
  "originalUrl": "https://www.example.com"
}
```

Example response (`201 Created`):

```json
{
  "shortCode": "aB12xYz9",
  "shortUrl": "http://localhost:8080/aB12xYz9",
  "originalUrl": "https://www.example.com",
  "expiresAt": null
}
```

The short code and response values are examples; the actual values depend on the application.

### 2. Create a Short URL With a Custom Alias

```json
{
  "originalUrl": "https://www.example.com",
  "customAlias": "my-link"
}
```

The application can use `my-link` as the short code if it is valid and available.

### 3. Create a Short URL With an Expiration Date

```json
{
  "originalUrl": "https://www.example.com",
  "expiresAt": "2027-01-01T12:00:00"
}
```

The expiration date must be in the future.

### 4. Redirect to the Original URL

**Endpoint:** `GET /{shortCode}`

Example:

```text
http://localhost:8080/aB12xYz9
```

The application redirects the user to the original URL if the short code exists and has not expired.

## Error Handling

The application is designed to handle common cases, including:

- Invalid URL input
- Missing required fields
- Duplicate custom aliases
- Unknown short codes
- Expired URLs

Depending on the configured exception handling, these cases may return HTTP statuses such as `400 Bad Request`, `404 Not Found`, `409 Conflict`, or `410 Gone`.

## Testing

Run the automated tests using the Maven Wrapper:

**Windows:**

```powershell
.\mvnw.cmd test
```

If the Maven Wrapper is unavailable, use:

```bash
mvn test
```

The test suite may cover:

- URL creation and validation
- Short code lookup
- Duplicate alias handling
- URL redirection
- Repository persistence
- MySQL integration using Testcontainers

Docker must be available for tests that use Testcontainers.

## Future Enhancements

- Redis caching for faster URL lookups
- URL click analytics and usage statistics
- User registration and authentication
- A dashboard to manage shortened URLs
- Rate limiting and abuse prevention
- Deployment to a cloud platform
- Monitoring and logging

## Learning Outcomes

This project provides practical experience with:

- Building REST APIs using Spring Boot
- Layered architecture: Controller, Service, Repository, and Entity
- Database operations using Spring Data JPA
- Input validation and exception handling
- HTTP redirects and status codes
- Automated unit and integration testing
- Docker-based testing and infrastructure
- Caching and performance optimization as a future enhancement

## License

This project is intended for learning and portfolio purposes. Add a license file if you decide to distribute it under a specific open-source license.
