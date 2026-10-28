# Repository guide

VetHub is a pet-clinic management app for owners, pets, visits and vets, with a SvelteKit frontend and Spring Boot API.

## Projects and commands

- `mise.toml` pins Java 25, Bun 1.3.0 and Node 22.23.4. Projects are independent: run Gradle commands in `server/` and Bun commands in `client/`.
- Server: `./gradlew bootRun`, `./gradlew test`, or `./gradlew test --tests 'dev.ilionx.workshop.api.owner.controller.OwnerControllerTest'` (append `.methodName` for one test). `./gradlew check` runs the configured quality checks; `./gradlew spotlessApply` formats Java.
- Any Java compilation depends on `spotlessApply`, so even tests/builds can modify source files. Compilation uses `-Werror`; MapStruct implementations are generated under `server/build/`.
- Client: `bun install`, `bun run dev`, `bun run check`, `bun run build`. There is no client test or lint script in `package.json`.

## Wiring and data

- Spring entrypoint is `server/src/main/java/dev/ilionx/workshop/Application.java`; feature-first domain packages under `api/` contain controllers, services and repositories. Use separate request/response DTOs and MapStruct mappers between entities and API models. API paths are centralized in `api/Paths.java`; the running server adds the `/api` servlet context.
- SvelteKit routes live in `client/src/routes/`; `$lib/api/*/*Controller.ts` wraps the typed `openapi-fetch` client, and `$lib/api/models.ts` re-exports generated schema types. `VITE_SERVER_BASE_URL` defaults to `http://localhost:8080` and must omit `/api`, which the client appends. Client Basic Auth defaults to `user`/`password` (override with `VITE_API_USERNAME`/`VITE_API_PASSWORD`).
- Dev uses in-memory H2; the dev profile enables Liquibase `drop-first` and the `tst` seed context. Changesets under `server/src/main/resources/db/changelog/changesets/` are included automatically. Integration tests use the `test` profile, H2 and `tst` seed data; `IntegrationTest` cleans most data around each test but preserves seeded vets, pet types and specialties.
- Extend `UnitTest` for Mockito-based unit tests and `IntegrationTest` for Spring context, repository and MockMvc integration tests; both provide reusable fixtures.

## OpenAPI types

- `client/src/lib/types/api.d.ts` is generated and tracked; regenerate after server API changes instead of editing it directly. With the server running on port 8080, from `client/` run `bun run download:api` then `bun run generate:api`, followed by `bun run check`. The download creates `server/openapi.json`, which is not ignored or tracked.
- `bun run sync:api` attempts to start its own server and stops Gradle daemons. Its readiness probe uses `/api/actuator/health`, whereas `application.yml` configures `/api/v1/public/actuator/health`; use the manual sequence above if sync fails. The script also removes any existing `server/openapi.json` before downloading.
