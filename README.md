# Spring Boot + React Full-Stack Template

A production-ready monorepo template for building full-stack applications with **Spring Boot** (Java) and **React** (TypeScript). Uses **OpenAPI** to auto-generate a typed API client for the frontend — no manual API code needed.

---

## Table of Contents

- [How It Works](#how-it-works)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Generate the TypeScript Client](#1-generate-the-typescript-client)
  - [2. Run the Backend](#2-run-the-backend)
  - [3. Run the Frontend](#3-run-the-frontend)
- [Production Build (Docker)](#production-build-docker)
- [Architecture Deep Dive](#architecture-deep-dive)
  - [OpenAPI Contract Flow](#openapi-contract-flow)
  - [Maven Build Phases](#maven-build-phases)
  - [Dockerfile Stages](#dockerfile-stages)
- [Key Files Explained](#key-files-explained)
- [Adding a New API Endpoint](#adding-a-new-api-endpoint)
- [API Reference](#api-reference)
- [FAQ & Troubleshooting](#faq--troubleshooting)

---

## How It Works

```
┌─────────────────────────────────────────────────────────────────┐
│  You write Java controllers with annotations                    │
│           ↓                                                     │
│  springdoc scans them → generates openapi.json                  │
│           ↓                                                     │
│  openapi-generator reads the spec → generates TypeScript client │
│           ↓                                                     │
│  React app imports the generated client → fully typed API calls │
└─────────────────────────────────────────────────────────────────┘
```

**One command** (`mvnw verify`) does all of this automatically. You never write fetch calls or maintain API types by hand.

---

## Project Structure

```
project-root/
├── src/                          ← Java backend (Spring Boot)
│   ├── main/java/com/.../
│   │   ├── SpringBootFullStackApplication.java   ← Entry point
│   │   ├── SampleDataController.java            ← Sample REST controller (/api/user)
│   │   └── Frontend.java                        ← SPA forwarding controller
│   └── main/resources/
│       ├── application.properties
│       └── static/               ← Built frontend output (gitignored)
│
├── frontend/                     ← React frontend (Vite + TypeScript)
│   ├── src/
│   │   ├── App.tsx               ← React app
│   │   ├── api.ts                ← API client setup
│   │   └── generated/            ← Auto-generated (DO NOT EDIT)
│   │       ├── apis/             ← Generated API classes
│   │       └── models/           ← Generated TypeScript types
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts            ← Dev proxy + builds to src/main/resources/static
│
├── pom.xml                       ← Maven build (backend + codegen)
├── Dockerfile                    ← Multi-stage production build
├── .dockerignore
└── .gitignore
```

---

## Tech Stack

| Layer    | Technology                                 | Purpose                              |
| -------- | ------------------------------------------ | ------------------------------------ |
| Backend  | Spring Boot 4.0.3, Java 17                 | REST API                             |
| Frontend | React 19, Vite 7, TypeScript 5.9           | Single-page app                      |
| Contract | springdoc-openapi 3.0.1                    | Generates `openapi.json` at runtime  |
| Codegen  | openapi-generator-maven-plugin 7.20        | Generates TypeScript-fetch client    |
| Build    | Maven (backend + codegen), pnpm (frontend) | Single `mvnw verify` does everything |
| Deploy   | Docker multi-stage → single JAR            | API + frontend in one container      |

---

## Prerequisites

| Tool       | Version | Check                    |
| ---------- | ------- | ------------------------ |
| Java (JDK) | 17+     | `java -version`          |
| Node.js    | 22+     | `node -v`                |
| pnpm       | 10+     | `pnpm -v`                |
| Docker     | 20+     | `docker -v` _(optional)_ |

> Maven wrapper (`mvnw` / `mvnw.cmd`) is included — no global Maven install needed.

---

## Getting Started

### 1. Generate the TypeScript Client

From the project root:

```bash
./mvnw clean verify -DskipTests        # Linux/macOS
mvnw.cmd clean verify -DskipTests      # Windows
```

**What this does** (in order):

1. Compiles the Spring Boot app
2. Starts it temporarily on port 8080
3. Fetches `http://localhost:8080/v3/api-docs` → writes `frontend/openapi.json`
4. Reads `openapi.json` → generates TypeScript client into `frontend/src/generated/`
5. Builds the React frontend into `src/main/resources/static/`
6. Stops the app

After this, `frontend/src/generated/` contains fully-typed API classes and models, and the built frontend is ready to be served by Spring Boot.

### 2. Run the Backend

```bash
./mvnw spring-boot:run
```

Backend runs at **http://localhost:8080**.

- API endpoints: `http://localhost:8080/api/user`
- Swagger UI: **http://localhost:8080/swagger-ui/index.html**

### 3. Run the Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs at **http://localhost:5173**.

Vite proxies `/api/*` requests to `localhost:8080`, so the frontend talks to the backend seamlessly during development.

---

## Production Build (Docker)

Build and run the entire app as a **single container**:

```bash
docker build -t spring-full-stack .
docker run -p 8080:8080 spring-full-stack
```

Open **http://localhost:8080** — serves both the API and the React frontend from one JAR.

### What the Docker Build Does

| Stage        | Base Image                      | Purpose                                                               |
| ------------ | ------------------------------- | --------------------------------------------------------------------- |
| 1. `build`   | `maven:3.9-eclipse-temurin-17`  | Run `mvn verify` → generate openapi.json + TS client + build frontend |
| 2. `runtime` | `eclipse-temurin:17-jre-alpine` | Minimal image (~92MB compressed), runs `app.jar`                      |

---

## Architecture Deep Dive

### OpenAPI Contract Flow

```
Java Controller (annotations)
				│
				▼
springdoc-openapi (runtime scan)
				│
				▼
/v3/api-docs endpoint (JSON)
				│
				▼
springdoc-openapi-maven-plugin
	(fetches spec during mvn integration-test)
				│
				▼
frontend/openapi.json
				│
				▼
openapi-generator-maven-plugin
	(generates TypeScript-fetch client)
				│
				▼
frontend/src/generated/
	├── apis/SampleDataControllerApi.ts
	├── models/User.ts
	└── runtime.ts
```

### Maven Build Phases

The `mvn verify` lifecycle triggers these plugins in order:

| Phase                   | Plugin                         | What It Does                                                  |
| ----------------------- | ------------------------------ | ------------------------------------------------------------- |
| `initialize`            | frontend-maven-plugin          | Installs Node.js and pnpm                                     |
| `generate-sources`      | frontend-maven-plugin          | Installs frontend dependencies                                |
| `compile`               | maven-compiler-plugin          | Compiles Java sources                                         |
| `pre-integration-test`  | spring-boot-maven-plugin       | **Starts** the app in background                              |
| `integration-test`      | springdoc-openapi-maven-plugin | Fetches `/v3/api-docs` → writes `frontend/openapi.json`       |
| `post-integration-test` | openapi-generator-maven-plugin | Reads spec → generates TS client to `frontend/src/generated/` |
| `verify`                | frontend-maven-plugin          | Builds React frontend into `src/main/resources/static/`       |
| `package`               | spring-boot-maven-plugin       | Packages everything into `app.jar`                            |

### Dockerfile Stages

```
Stage 1 (build)
		mvn verify (backend, openapi.json, TS client, frontend build)
		↓
		target/app.jar

Stage 2 (runtime)
		COPY app.jar
		java -jar app.jar    ← ~92MB compressed image
```

---

## Key Files Explained

### `pom.xml`

The Maven build configuration. Key sections:

- **Dependencies**: `spring-boot-starter-web` (REST), `springdoc-openapi` (spec generation), `devtools` (hot reload)
- **frontend-maven-plugin**: Installs Node.js, pnpm, runs frontend build
- **spring-boot-maven-plugin**: Starts/stops the app during integration-test for spec generation
- **springdoc-openapi-maven-plugin**: Fetches the OpenAPI spec from the running app
- **openapi-generator-maven-plugin**: Converts the spec into a TypeScript-fetch client

### `frontend/vite.config.ts`

Configures the dev proxy so frontend requests to `/api/*` are forwarded to the Spring Boot backend on port 8080. Also sets the build output to `../src/main/resources/static` so the built frontend is served directly by Spring Boot.

### `frontend/src/api.ts`

Sets up the generated API client with an empty `basePath` (so it uses relative URLs, which work with both the Vite proxy in dev and the same-origin JAR in production).

### `frontend/src/generated/` (auto-generated)

**Do not edit these files.** They are regenerated every time you run `mvn verify`. The `.gitignore` excludes them from version control.

### `frontend/tsconfig.app.json`

TypeScript config with `noUnusedLocals` and `noUnusedParameters` set to `false` to accommodate the auto-generated code (which sometimes has unused imports/params).

---

## Adding a New API Endpoint

**Step 1:** Create a Java controller:

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

		public record OrderResponse(Long id, String item, int quantity) {}

		@GetMapping
		public List<OrderResponse> getOrders() {
				return List.of(new OrderResponse(1L, "Widget", 5));
		}
}
```

**Step 2:** Regenerate the TypeScript client:

```bash
./mvnw clean verify -DskipTests
```

**Step 3:** Use it in React:

```typescript
// api.ts
import { OrderControllerApi, Configuration } from "./generated";
const config = new Configuration({ basePath: "" });
export const orderApi = new OrderControllerApi(config);

// Component
const [orders, setOrders] = useState<OrderResponse[]>([]);
useEffect(() => {
  orderApi.getOrders().then(setOrders);
}, []);
```

That's it. Full type safety, zero manual API code.

---

## API Reference

| Method | Path           | Description           |
| ------ | -------------- | --------------------- |
| GET    | `/api/user`    | Get a sample user     |
| GET    | `/swagger-ui/` | Interactive API docs  |
| GET    | `/v3/api-docs` | Raw OpenAPI JSON spec |

---

## FAQ & Troubleshooting

### `mvnw verify` fails — "Address already in use: bind" on port 8080

Another process is using port 8080. Stop it first:

```bash
# Find the process
lsof -i :8080           # Linux/macOS
netstat -ano | find "8080"  # Windows

# Kill it, then retry
```

### Generated code has TypeScript errors

The generated code uses `enum` syntax and may have unused imports. The `tsconfig.app.json` is already configured to allow this (`noUnusedLocals: false`, `noUnusedParameters: false`). If you see errors, make sure you haven't re-enabled those flags.

### Vite proxy not working — CORS errors

Make sure the backend is running on port 8080 before starting `pnpm dev`. The proxy in `vite.config.ts` forwards `/api` to `http://localhost:8080`.

### Docker build fails with TLS errors

Docker Desktop sometimes has certificate issues. Restart Docker Desktop and try again. You can also pre-pull images manually:

```bash
docker pull eclipse-temurin:17-jdk
docker pull maven:3.9-eclipse-temurin-17
docker pull eclipse-temurin:17-jre-alpine
```

### How do I change the Java version?

Update both:

1. `pom.xml` → `<java.version>17</java.version>`
2. `Dockerfile` → all `eclipse-temurin:17-*` tags

### Where does the frontend go in production?

Vite builds directly into `src/main/resources/static/`. Spring Boot serves static files from `/static` automatically, so `/` serves `index.html` without any explicit controller.
