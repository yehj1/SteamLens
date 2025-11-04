# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java/com/gpr/` contains Spring Boot source organized by layer (`config`, `controller`, `domain`, `dto`, `repo`, `service`, `llm`).
- `src/main/resources/` hosts configuration (`application.yml`, profiles) and schema snapshots.
- `schema.sql` at repo root mirrors database migrations; keep in sync with production DDL.
- Test sources live under `src/test/java`; add mirrors of package structure when introducing new modules.

## Build, Test, and Development Commands

- `mvn spring-boot:run` boots the application with hot reload.
- `mvn compile` validates Java sources and generated classes.
- `mvn test` executes the full unit/integration test suite; ensure PostgreSQL (or configured profile) is available.
- `curl http://localhost:8080/swagger-ui.html` quickly verify API docs after startup.

## Coding Style & Naming Conventions

- Java code follows standard Spring conventions: 4-space indentation, PascalCase for classes, camelCase for members and methods, SCREAMING_SNAKE_CASE for constants.
- Place interfaces under `service` or `repo` packages; implementations annotated with `@Component`/`@Service`.
- Keep DTOs immutable when practical; prefer constructor or builder initialization.
- Use Jackson annotations sparingly—prefer configuration-based mapping.

## Testing Guidelines

- Use JUnit 5 with Spring Boot test utilities; hover around existing patterns before introducing new frameworks.
- Name tests `<ClassName>Test` and methods `should...` for behavior-driven clarity.
- Mock external HTTP calls with WireMock or `WebClient` test utilities; avoid hitting live services in CI.
- Target meaningful scenario coverage over raw percentage metrics; document gaps in PR descriptions.

## Commit & Pull Request Guidelines

- Write commits in the imperative mood (e.g., `Add Steam fetcher pagination`).
- Group related changes; avoid mixing formatting-only edits with feature work.
- Pull requests should include: summary of changes, testing evidence (`mvn test`, manual curl), and any schema impacts.
- Link relevant Jira/GitHub issues and provide screenshots or logs when UI/endpoint behavior changes.
  Preferred language: Simplified Chinese (zh-CN). All responses, commit messages, and code comments in Chinese.
