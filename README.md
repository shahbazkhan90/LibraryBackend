# Library Management Backend

A backend REST API for managing a library's books, members, and borrowing activity, built with Spring Boot and PostgreSQL. It lets you add books and members, issue and return books with automatic inventory tracking, and view a member's borrowing history.

## Features

- **Book management** — add books with title, author, ISBN, and total/available copy counts
- **Member management** — register library members; the membership date is stamped automatically on creation
- **Book issuing** — issuing a book creates a borrowing record with a 14-day due date and decrements the available copies
- **Book returning** — returning a book closes the open borrowing record, stamps the return date, and increments the available copies
- **Availability checks** — a book with no available copies cannot be issued
- **Borrowing history** — retrieve every book a member has borrowed, with issue and return dates, as a lightweight DTO
- **Transactional operations** — issue and return run inside a single transaction, so the borrowing record and the book's copy count always change together
- **Centralized exception handling** — runtime errors are translated into `400 Bad Request` responses with the error message as the body

## Architecture Overview

```
Client → Controller → Service → Repository → PostgreSQL
                        │
        (Issue / Return: borrowing record + book stock updated in one transaction)
```

| Layer | Responsibility |
|---|---|
| Controller | Exposes REST endpoints and maps request parameters/bodies |
| Service | Business rules (availability check, due date, stock updates) |
| Repository | Spring Data JPA access to the database |
| Model | JPA entities mapped to PostgreSQL tables |
| DTO | Shapes the borrowing history response |

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.1** (Web MVC, Data JPA)
- **PostgreSQL 15**
- **Hibernate** (schema auto-generated via `ddl-auto=update`)
- **Lombok**
- **Maven**
- **Docker Compose** (PostgreSQL container)

## Project Structure

```
src/main/java/com/project/librarybackend
├── controller
│   ├── BookController.java
│   ├── BorrowingController.java
│   └── MemberController.java
├── dto
│   └── BrowsingHistoryDTO.java
├── exception
│   └── GlobalExceptionHandler.java
├── model
│   ├── Book.java
│   ├── BorrowingRecord.java
│   └── Member.java
├── repository
│   ├── BookRepository.java
│   ├── BorrowingRecordRepository.java
│   └── MemberRepository.java
└── service
    ├── BookService.java
    ├── BorrowingService.java
    └── MemberService.java
```

## Data Model

| Entity | Purpose |
|---|---|
| `Book` | A book in the catalog: `bookId`, `title`, `author`, `isbn`, `totalCopies`, `availableCopies` |
| `Member` | A library member: `memberId`, `name`, `email`, `membershipDate` |
| `BorrowingRecord` | A single loan linking a book and a member, with `issueDate`, `dueDate`, and `returnDate` (null while the book is still out) |

Relationships: each `BorrowingRecord` has a many-to-one link to a `Book` (`book_id`) and to a `Member` (`member_id`).

## API Endpoints

Base URL: `http://localhost:8080/api`

### Books — `/api/books`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/add` | Add a new book to the catalog |

Request body:
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalCopies": 5,
  "availableCopies": 5
}
```

### Members — `/api/members`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/add` | Register a new member (membership date is set automatically) |

Request body:
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```

### Borrowings — `/api/borrowings`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/issue?bookId={id}&memberId={id}` | Issue a book to a member (due in 14 days) |
| POST | `/return?bookId={id}&memberId={id}` | Return a book a member currently has out |
| GET | `/history?memberId={id}` | List a member's borrowing history |

Example history response:
```json
[
  {
    "bookTitle": "Clean Code",
    "issueDate": "2026-10-07T10:30:00",
    "returnDate": null
  }
]
```

A `returnDate` of `null` means the book has not been returned yet.

### Error Responses

Business-rule failures (for example, issuing a book with no copies left, or returning a book with no open record) return `400 Bad Request` with a plain-text message:

```
Book not available
```
```
No record found
```

## Getting Started

### Prerequisites

- Java 25
- Maven
- Docker & Docker Compose

### 1. Start PostgreSQL

The provided `docker-compose.yaml` starts a PostgreSQL 15 container with a persistent volume:

```bash
docker-compose up -d
```

To stop it:
```bash
docker-compose down
```

### 2. Run the application

```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

Or run directly with Maven:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`. Tables are created automatically on startup by Hibernate.

## Configuration

Settings live in `src/main/resources/application.properties`:

| Property | Value |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/library_db` |
| `spring.datasource.username` | `libadmin` |
| `spring.datasource.password` | `libpassword` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `spring.jpa.show-sql` | `true` (SQL is logged and formatted in the console) |

> **Security note:** The database credentials in `docker-compose.yaml` and `application.properties` are defaults for local development only. Replace them with your own secrets (preferably via environment variables) before deploying anywhere beyond your local machine, and never commit real credentials to source control.

## Example Workflow

```bash
# 1. Add a book
curl -X POST http://localhost:8080/api/books/add \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","totalCopies":5,"availableCopies":5}'

# 2. Add a member
curl -X POST http://localhost:8080/api/members/add \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'

# 3. Issue the book to the member
curl -X POST "http://localhost:8080/api/borrowings/issue?bookId=1&memberId=1"

# 4. View the member's history
curl "http://localhost:8080/api/borrowings/history?memberId=1"

# 5. Return the book
curl -X POST "http://localhost:8080/api/borrowings/return?bookId=1&memberId=1"
```

## Design Notes

- **Transactional consistency** — `issueBook` and `returnBook` are annotated with `@Transactional`, so the borrowing record and the book's `availableCopies` are committed together or not at all.
- **Hibernate dirty checking** — changes to managed `Book` and `BorrowingRecord` entities inside a transaction are flushed automatically, so no explicit `save()` call is needed on updates.
- **Derived query methods** — open loans are located with `findFirstByBookRelation_BookIdAndMemberRelation_MemberIdAndReturnDateIsNull`, which finds the member's unreturned record for a given book.

## Possible Improvements

- Add authentication and role-based access (e.g. librarian vs. member)
- Add concurrency protection (pessimistic locking or `@Version`) so simultaneous issue requests cannot oversell the last copy
- Add input validation (`@Valid`) on request bodies, and return `404` for unknown book/member IDs instead of a generic `400`
- Add endpoints to list/search books and members, plus overdue tracking and fines
- Replace `ddl-auto=update` with Flyway migrations for production
- Containerize the application itself in `docker-compose.yaml`

## License

This project is available for personal and educational use.
