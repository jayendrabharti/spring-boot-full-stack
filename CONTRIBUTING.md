# Contributing Guide

Thank you for your interest in contributing!

## Local Development Setup

1. **Clone the repo:**
   ```bash
   git clone <repo-url>
   cd spring-boot-full-stack
   ```
2. **Install prerequisites:**
   - Java 17+
   - Node.js 22+
   - pnpm 10+
   - Docker (optional, for prod build)
3. **Install frontend dependencies:**
   ```bash
   cd frontend
   pnpm install
   ```
4. **Run backend:**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Run frontend (in another terminal):**
   ```bash
   cd frontend
   pnpm dev
   ```

   - The frontend runs at http://localhost:5173 and proxies API requests to http://localhost:8080.

## Adding a New API Endpoint

1. **Create a new Java controller or add to an existing one.**
   - All REST endpoints should be under `/api/*`.
2. **Regenerate the OpenAPI spec and TypeScript client:**
   ```bash
   ./mvnw clean verify -DskipTests
   ```

   - This will update `frontend/openapi.json` and `frontend/src/generated/`.
3. **Use the generated client in React:**
   - Import from `frontend/src/generated/`.
   - Use the API classes and models directly for type-safe calls.

## Code Conventions

- **Java:**
  - Use `@RestController` and `@RequestMapping("/api/...")` for all API endpoints.
  - Use Java records for DTOs where possible.
- **TypeScript:**
  - Use the generated API client for all backend calls.
  - Do not edit files in `frontend/src/generated/`.
- **Frontend:**
  - Use React functional components and hooks.
  - Keep business logic in hooks or context, not in UI components.

## Build & Production

- **Full build:**
  ```bash
  ./mvnw clean verify -DskipTests
  ```

  - This generates the OpenAPI spec, TypeScript client, builds the frontend, and packages everything into `target/app.jar`.
- **Docker build:**
  ```bash
  docker build -t myapp .
  docker run -p 8080:8080 myapp
  ```

## Troubleshooting

- If the TypeScript client is out of sync, always re-run `./mvnw verify`.
- If you see CORS errors in dev, make sure both backend and frontend are running.
- If port 8080 is in use, stop the other process or change the port in `application.properties` and Vite proxy.

---

For questions, open an issue or start a discussion!
