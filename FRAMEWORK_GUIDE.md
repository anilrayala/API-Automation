# API Automation Framework — Complete Technical Reference

> **Audience:** Anyone reading the codebase for the first time. This document covers every
> file, every test method, every configuration key, and every data-flow path through the
> framework. No prior knowledge of the project is assumed.

---

## Table of Contents

1. [Framework Overview](#1-framework-overview)
2. [File-by-File Reference](#2-file-by-file-reference)
   - [Main Sources — config](#21-mainjavaorgapiautomationconfig)
   - [Main Sources — constants](#22-mainjavaorgapiautomationconstants)
   - [Main Sources — data](#23-mainjavaorgapiautomationdata)
   - [Main Sources — models/request](#24-mainjavaorgapiautomationmodelsrequest)
   - [Main Sources — models/response](#25-mainjavaorgapiautomationmodelsresponse)
   - [Main Sources — utils](#26-mainjavaorgapiautomationutils)
   - [Test Sources — base](#27-testjavaorgapiautomationbase)
   - [Test Sources — listeners](#28-testjavaorgapiautomationlisteners)
   - [Test Sources — bdd](#29-testjavaorgapiautomationbdd)
   - [Test Sources — tests](#210-testjavaorgapiautomationtests)
3. [Test Execution Flows](#3-test-execution-flows)
4. [BDD (Cucumber) Flow](#4-bdd-cucumber-flow)
5. [Configuration and Secrets Resolution](#5-configuration-and-secrets-resolution)
6. [Data Sources](#6-data-sources)
7. [Request/Response POJO Catalogue](#7-requestresponse-pojo-catalogue)
8. [Test Suites](#8-test-suites)

---

## 1. Framework Overview

### Technology Stack

| Layer | Library | Version |
|---|---|---|
| HTTP / assertion | REST Assured | 5.5.6 |
| Unit runner | TestNG | 7.10.2 |
| BDD runner | Cucumber (TestNG adapter) | 7.34.0 |
| BDD DI | cucumber-picocontainer | 7.34.0 |
| Serialization | Jackson Databind | 2.18.2 |
| Spreadsheet | Apache POI (poi-ooxml) | 5.4.0 |
| Assertions (fluent) | Hamcrest | 3.0 |
| Logging | Log4j2 | 2.24.3 |
| Build | Maven + Surefire | 3.5.2 |
| Java target | Java 21 (LTS) | — |

### Package Structure

```
src/
├── main/
│   ├── java/org/api/automation/
│   │   ├── config/
│   │   │   ├── ConfigKeys.java          — String constants for every config key
│   │   │   └── ConfigManager.java       — 3-level property resolver (system → env → file)
│   │   ├── constants/
│   │   │   └── Paths.java               — All API endpoint path strings
│   │   ├── data/
│   │   │   ├── ExcelDataReader.java     — Reads .xlsx rows as header-keyed Maps
│   │   │   ├── GraphQLQueries.java      — GraphQL document constants (introspection-based)
│   │   │   └── TestDataFactory.java     — Builds request POJOs with sensible defaults
│   │   ├── models/
│   │   │   ├── request/                 — Jackson-serialized request body POJOs
│   │   │   │   ├── AddBookRequest.java
│   │   │   │   ├── AddPlaceRequest.java
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── DeleteBookRequest.java
│   │   │   │   ├── DeletePlaceRequest.java
│   │   │   │   ├── GraphQLRequest.java
│   │   │   │   ├── Location.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── OrderDetail.java
│   │   │   │   └── UpdatePlaceRequest.java
│   │   │   └── response/                — Jackson-deserialized response body POJOs
│   │   │       ├── AddBookResponse.java
│   │   │       ├── AddPlaceResponse.java
│   │   │       ├── AddProductResponse.java
│   │   │       ├── Course.java
│   │   │       ├── CourseCatalog.java
│   │   │       ├── CourseDetailsResponse.java
│   │   │       ├── CreateOrderResponse.java
│   │   │       ├── GeoLocation.java
│   │   │       ├── GetPlaceResponse.java
│   │   │       ├── GraphQLResponse.java
│   │   │       ├── LoginResponse.java
│   │   │       ├── MessageResponse.java
│   │   │       └── OAuthTokenResponse.java
│   │   └── utils/
│   │       ├── JsonUtils.java           — JsonPath extraction + Jackson helpers
│   │       └── ResourceReader.java      — Classpath resource loading + template substitution
│   └── resources/
│       └── payloads/
│           ├── add-book.json            — Payload template with {{isbn}} and {{aisle}}
│           ├── add-place.json           — Static example payload (no placeholders)
│           └── delete-book.json         — Payload template with {{bookId}}
└── test/
    ├── java/org/api/automation/
    │   ├── base/
    │   │   └── BaseTest.java            — Abstract base: spec builders + traffic log
    │   ├── listeners/
    │   │   └── TestListener.java        — TestNG ITestListener: logs each test's result
    │   ├── bdd/
    │   │   ├── context/
    │   │   │   └── ScenarioContext.java — Per-scenario state bag (PicoContainer-managed)
    │   │   ├── runner/
    │   │   │   └── CucumberTestRunner.java — TestNG-based Cucumber runner
    │   │   └── stepdefinitions/
    │   │       ├── Hooks.java           — @Before / @After lifecycle hooks
    │   │       └── PlaceStepDefinitions.java — Given/When/Then for Places feature
    │   └── tests/
    │       ├── auth/
    │       │   └── OAuthTest.java       — OAuth 2.0 token + protected-resource tests
    │       ├── ecom/
    │       │   └── EcomOrderE2ETest.java — E2E: login → product → order → cleanup
    │       ├── graphql/
    │       │   └── GraphQLTest.java     — GraphQL introspection + error-envelope tests
    │       ├── json/
    │       │   └── JsonPathParsingTest.java — Complex JsonPath / GPath offline tests
    │       ├── library/
    │       │   ├── BookPayloadTest.java  — 4 payload-construction styles (POJO/template/Map/DD)
    │       │   └── ExcelDataDrivenTest.java — Excel-driven book add + reader validation
    │       ├── place/
    │       │   ├── AddPlaceDataDrivenTest.java — Parameterised Add Place via @DataProvider
    │       │   ├── AddPlaceTest.java     — Add Place: fluent + extract-then-assert styles
    │       │   └── PlaceCrudE2ETest.java — Full CRUD lifecycle: create→get→update→get→delete
    │       └── serialization/
    │           └── SerializationTest.java — POJO↔JSON offline mapping tests
    └── resources/
        ├── config/
        │   └── qa.properties            — QA environment base URLs and non-secret settings
        ├── features/
        │   └── placeValidations.feature — BDD scenarios for Places (create, retrieve, delete)
        ├── suites/
        │   ├── testng.xml               — Default suite (all tests minus credentials + unverified)
        │   ├── smoke.xml                — Fast subset: smoke-tagged tests only
        │   ├── offline.xml              — No-network subset: offline-tagged tests only
        │   └── auth.xml                 — Credential-dependent: OAuth + E-com tests
        ├── testdata/
        │   ├── course-price.json        — 4-course JSON for JsonPath parsing exercises
        │   ├── oauth-course-details-sample.json — Sample OAuth response for offline deserialization
        │   ├── TestData.xlsx            — Spreadsheet with AddBook and Login rows
        │   └── laptop.jpg               — Binary fixture for multipart product-image upload
        └── log4j2.xml                   — Log4j2 configuration: Console + File appenders
```

### Design Principles

| Principle | How it is applied |
|---|---|
| No hardcoded values | `ConfigManager` reads every URL, key, and credential from a three-level lookup chain. |
| No raw JSON strings | Request bodies are POJOs; Jackson serializes them. The models are the documentation. |
| No shared mutable state between specs | `BaseTest` spec builders return a fresh `RequestSpecification` on every call. |
| Responses should tolerate API additions | All response POJOs carry `@JsonIgnoreProperties(ignoreUnknown = true)`. |
| Repeatable tests | Book ISBNs and place names that must be unique are generated with a timestamp + random suffix via `TestDataFactory.uniqueSuffix()`. |
| Separate request and response models | Avoids the asymmetry problem: this API accepts `lat`/`lng` but returns `latitude`/`longitude`. |
| GraphQL errors are in the body, not the status | `GraphQLResponse.hasErrors()` is always checked; status code alone is insufficient. |

---

## 2. File-by-File Reference

---

### 2.1 `main/java/org/api/automation/config`

#### `ConfigKeys.java`

**Full path:** `src/main/java/org/api/automation/config/ConfigKeys.java`

**Role:** A non-instantiable constants class. Every configuration key the framework reads is declared here as a `public static final String`. A typo in a key becomes a compile error rather than a silent `null` at runtime, and "find usages" shows every site that reads a particular setting.

**Fields:**

| Constant | Property Key | Purpose |
|---|---|---|
| `PLACES_BASE_URL` | `places.baseUrl` | Root URI for the Google Places practice API |
| `LIBRARY_BASE_URL` | `library.baseUrl` | Root URI for the Library API |
| `ECOM_BASE_URL` | `ecom.baseUrl` | Root URI for the e-commerce API |
| `OAUTH_BASE_URL` | `oauth.baseUrl` | Root URI for the OAuth practice API |
| `GRAPHQL_BASE_URL` | `graphql.baseUrl` | Root URI for the GraphQL endpoint |
| `GOOGLE_TOKEN_URL` | `google.token.url` | Google OAuth2 token endpoint (for authorization-code flow reference) |
| `PLACES_API_KEY` | `places.apiKey` | API key appended to every Places request as `?key=` |
| `ECOM_USER_EMAIL` | `ecom.userEmail` | Login email for the e-commerce API (secret — supply via env var) |
| `ECOM_USER_PASSWORD` | `ecom.userPassword` | Login password for the e-commerce API (secret — supply via env var) |
| `OAUTH_CLIENT_ID` | `oauth.clientId` | OAuth client ID (secret — supply via env var) |
| `OAUTH_CLIENT_SECRET` | `oauth.clientSecret` | OAuth client secret (secret — supply via env var) |
| `OAUTH_SCOPE` | `oauth.scope` | OAuth scope string (default: `trust`) |
| `OAUTH_REDIRECT_URI` | `oauth.redirectUri` | Redirect URI for authorization-code flow |
| `LOG_REQUESTS` | `framework.logRequests` | Whether to enable REST Assured traffic logging |
| `RETRY_COUNT` | `framework.retryCount` | Number of retries before a test is reported as failed |
| `RELAXED_HTTPS` | `framework.relaxedHttps` | Whether to skip SSL certificate validation |

**Used by:** `ConfigManager`, `BaseTest`, `OAuthTest`, `EcomOrderE2ETest`, `PlaceCrudE2ETest`, `Hooks`, `PlaceStepDefinitions`.

---

#### `ConfigManager.java`

**Full path:** `src/main/java/org/api/automation/config/ConfigManager.java`

**Role:** The single source of truth for every runtime value. Loads the environment's `.properties` file at class initialization, then for each `get()` call applies a three-level lookup so that secrets can live outside the file without any code changes.

**Resolution order (first match wins):**

1. JVM system property — `mvn test -Dplaces.baseUrl=https://staging.example.com`
2. OS environment variable — key upper-cased, dots replaced with underscores: `places.baseUrl` → `PLACES_BASEURL`
3. `config/<env>.properties` on the classpath (loaded once, shared across all calls)

**Environment selection:** The `env` system property selects the properties file. Default is `qa`, loading `config/qa.properties`. Override with `-Denv=staging` to load `config/staging.properties`.

**Key methods:**

| Method | Behaviour |
|---|---|
| `environment()` | Returns the active environment name (e.g. `"qa"`). |
| `get(String key)` | Returns the resolved value, or `null` if not set anywhere. |
| `get(String key, String defaultValue)` | Returns the resolved value, falling back to the supplied default. |
| `getRequired(String key)` | Returns the resolved value or throws `IllegalStateException` with a message naming the key, the `-D` flag, and the env-var form. Used for credentials. |
| `getInt(String key, int defaultValue)` | Parses the value as an integer. |
| `getBoolean(String key, boolean defaultValue)` | Parses the value as a boolean. |
| `toEnvVarName(String key)` | Converts `oauth.client.secret` → `OAUTH_CLIENT_SECRET`. |

**Used by:** `BaseTest`, `OAuthTest`, `EcomOrderE2ETest`, `PlaceCrudE2ETest`, `Hooks`, `PlaceStepDefinitions`.

---

### 2.2 `main/java/org/api/automation/constants`

#### `Paths.java`

**Full path:** `src/main/java/org/api/automation/constants/Paths.java`

**Role:** Centralizes every endpoint path string. A URL change is one edit here rather than a grep through test files. The `fromName()` factory translates the plain-English strings written in Gherkin steps (e.g. `"ADD_PLACE"`) into the actual path constant.

**Constants:**

| Constant | Value | API |
|---|---|---|
| `ADD_PLACE` | `/maps/api/place/add/json` | Google Places |
| `GET_PLACE` | `/maps/api/place/get/json` | Google Places |
| `UPDATE_PLACE` | `/maps/api/place/update/json` | Google Places |
| `DELETE_PLACE` | `/maps/api/place/delete/json` | Google Places |
| `ADD_BOOK` | `/Library/Addbook.php` | Library |
| `DELETE_BOOK` | `/Library/DeleteBook.php` | Library |
| `ECOM_LOGIN` | `/api/ecom/auth/login` | E-commerce |
| `ECOM_ADD_PRODUCT` | `/api/ecom/product/add-product` | E-commerce |
| `ECOM_CREATE_ORDER` | `/api/ecom/order/create-order` | E-commerce |
| `ECOM_DELETE_PRODUCT` | `/api/ecom/product/delete-product/{productId}` | E-commerce |
| `ECOM_DELETE_ORDER` | `/api/ecom/order/delete-order/{orderId}` | E-commerce |
| `OAUTH_TOKEN` | `/oauthapi/oauth2/resourceOwner/token` | OAuth practice API |
| `OAUTH_COURSE_DETAILS` | `/oauthapi/getCourseDetails` | OAuth practice API |
| `GRAPHQL` | `/gq/graphql` | GraphQL |

**`fromName(String name)`:** Takes an uppercase endpoint name as written in a Gherkin step (`"ADD_PLACE"`, `"DELETE_PLACE"`, etc.) and returns the matching path constant. A typo throws `IllegalArgumentException` that lists all valid names.

**Used by:** All test classes, `Hooks`, `PlaceStepDefinitions`.

---

### 2.3 `main/java/org/api/automation/data`

#### `ExcelDataReader.java`

**Full path:** `src/main/java/org/api/automation/data/ExcelDataReader.java`

**Role:** Reads `.xlsx` spreadsheet data and returns rows as `Map<String, String>` keyed by column header. Column-name access rather than positional index means an inserted column cannot silently shift data.

**Key design improvements over the original `ExcelReader`:**
- Workbook and `InputStream` are closed via try-with-resources (the original leaked a `FileInputStream`).
- Returns `Map<String, String>` keyed by header name, not `ArrayList<String>` by index.
- Reads through the classloader, not a relative `File` path, so it works from any working directory.
- `NumberToTextConverter` prevents numeric ISBNs like `12345` from arriving as `"12345.0"`.

**Key methods:**

| Method | Parameters | Behaviour |
|---|---|---|
| `getRow(resourcePath, sheetName, keyColumn, keyValue)` | Classpath path, sheet name, header of the lookup column, value to find | Returns the single row where `keyColumn` equals `keyValue` as a `Map`. Throws `IllegalArgumentException` if no match. |
| `getAllRows(resourcePath, sheetName)` | Classpath path, sheet name | Returns every data row (excluding the header row and blank rows) as a `List<Map<String, String>>`. Can be fed directly to a TestNG `@DataProvider`. |

**Spreadsheet format expected:** Row 0 is the header. Each subsequent non-blank row is a data row. Headers become map keys; values are always strings.

**Cell type handling in `cellToString()`:**

| Cell type | How it is converted |
|---|---|
| `STRING` | `getStringCellValue().trim()` |
| `NUMERIC` | `NumberToTextConverter.toText(numericValue)` (avoids `.0` suffix) |
| `NUMERIC` (date formatted) | `getLocalDateTimeCellValue().toString()` |
| `BOOLEAN` | `String.valueOf(booleanValue)` |
| `FORMULA` | `getCellFormula()` (the formula text, not the evaluated result) |
| Blank / null | `""` |

**Used by:** `ExcelDataDrivenTest`.

---

#### `GraphQLQueries.java`

**Full path:** `src/main/java/org/api/automation/data/GraphQLQueries.java`

**Role:** Holds every GraphQL query document as a named Java text-block constant. Centralizing them here means a schema change is one edit, and the constants are readable without escaping.

**All queries use GraphQL introspection** (`__schema`, `__type`) deliberately: they work against any spec-compliant GraphQL server without depending on a specific domain schema being live.

**Constants:**

| Constant | Purpose |
|---|---|
| `SCHEMA_QUERY_TYPE` | Minimal connectivity check — asks for the name of the root query type. |
| `DISCOVER_QUERY_FIELDS` | Lists every root query field with its arguments and return type. Run this first against a new endpoint to find what fields exist. |
| `DISCOVER_MUTATION_FIELDS` | Lists every mutation field, or returns `null` for the `mutationType` if the endpoint is read-only. |
| `TYPE_INFO_BY_NAME` | A parameterised query — takes `$typeName: String!` as a variable and returns `name`, `kind`, and `description` for that type. Demonstrates the variables mechanism. |
| `INVALID_FIELD` | References `thisFieldDoesNotExistAnywhere` — causes the server to return HTTP 200 with an `errors` array, demonstrating that status code alone is insufficient for GraphQL. |
| `MALFORMED_DOCUMENT` | An unclosed brace — causes a parse-time rejection, typically HTTP 400. |

**Used by:** `GraphQLTest`.

---

#### `TestDataFactory.java`

**Full path:** `src/main/java/org/api/automation/data/TestDataFactory.java`

**Role:** Static factory for fully populated request POJOs. Separating "what a valid payload looks like" from "what the test is checking" means adding a new required field to an API is one edit here, not one edit per test.

**Methods:**

| Method | Parameters | Returns | Notes |
|---|---|---|---|
| `addPlace(name, language, address)` | Three strings the tests typically vary | Fully populated `AddPlaceRequest` | Accuracy=50, phone, website, types, and location are defaulted. |
| `defaultPlace()` | — | `AddPlaceRequest` with all fields defaulted | Convenience for tests that do not care about content. |
| `uniqueBook(aisle)` | Aisle string | `AddBookRequest` with a unique isbn | ISBN is `"isbn" + uniqueSuffix()`. The Library API derives the book id from `isbn + aisle`, so a fixed ISBN fails on the second run. |
| `book(isbn, aisle)` | Both strings | `AddBookRequest` with a caller-supplied isbn | Used when the test asserts on the exact id. |
| `uniqueSuffix()` | — | `String` | `currentTimeMillis()` concatenated with a random int (1000–9999) for thread safety. |

**Used by:** `AddPlaceTest`, `AddPlaceDataDrivenTest`, `PlaceCrudE2ETest`, `BookPayloadTest`, `EcomOrderE2ETest`, `SerializationTest`, `Hooks`, `PlaceStepDefinitions`.

---

### 2.4 `main/java/org/api/automation/models/request`

All request POJOs follow the same conventions:
- No-arg constructor (required by Jackson for deserialization in tests that round-trip).
- All-arg constructor for convenient construction.
- Standard getters and setters.
- `@JsonProperty` when the API field name differs from the Java camelCase field name.

#### `AddBookRequest.java`

**Fields:** `name` (String), `isbn` (String), `aisle` (String), `author` (String).

**Convenience method:** `expectedBookId()` returns `isbn + aisle`, which is how the Library API derives the book id. Tests use this to assert on the returned id without hardcoding the formula twice.

**No Jackson annotations** — all four fields match the API names exactly.

---

#### `AddPlaceRequest.java`

**Fields:** `location` (Location), `accuracy` (int), `name` (String), `phoneNumber` (String), `address` (String), `types` (List\<String\>), `website` (String), `language` (String).

**Annotation:** `@JsonProperty("phone_number")` on `phoneNumber`. The API expects `phone_number` (snake_case) but the Java field is camelCase. Without this annotation, the outgoing JSON would contain `phoneNumber` and the API would silently ignore the value.

---

#### `CreateOrderRequest.java`

**Fields:** `orders` (List\<OrderDetail\>).

**Convenience factory:** `forSingleProduct(country, productId)` — creates a request with a single-element list, covering the common single-order case without requiring callers to build the list themselves.

---

#### `DeleteBookRequest.java`

**Fields:** `id` (String).

**Annotation:** `@JsonProperty("ID")` — the Library API requires the key to be uppercase `ID` in the request body.

---

#### `DeletePlaceRequest.java`

**Fields:** `placeId` (String).

**Annotation:** `@JsonProperty("place_id")` — maps the Java camelCase field to the snake_case wire name. Building this with string concatenation (as the old code did) silently breaks when the id contains a quote or backslash; Jackson escapes correctly.

---

#### `GraphQLRequest.java`

**Fields:** `query` (String), `variables` (Map\<String, Object\>), `operationName` (String).

**Role:** Represents the standard GraphQL request envelope. All three fields are optional in GraphQL over HTTP, but `query` is always required in practice. Variables keep the document text constant and avoid injection risks. `operationName` selects one operation when a document defines multiple.

**Constructors:** No-arg (Jackson), query-only, query-plus-variables.

---

#### `Location.java`

**Fields:** `lat` (double), `lng` (double).

**Role:** The nested `location` object in `AddPlaceRequest`. The Places API is asymmetric: it accepts `lat`/`lng` here but returns `latitude`/`longitude` in the Get Place response. That is why `GeoLocation` (in the response package) is a separate class.

---

#### `LoginRequest.java`

**Fields:** `userEmail` (String), `userPassword` (String).

**Security note:** `toString()` is overridden to mask the password with `****`. This prevents credentials appearing in test logs or assertion failure messages.

---

#### `OrderDetail.java`

**Fields:** `country` (String), `productOrderedId` (String).

**Role:** A single line item inside `CreateOrderRequest.orders`. No Jackson annotations — both field names match the API.

---

#### `UpdatePlaceRequest.java`

**Fields:** `placeId` (String), `address` (String), `key` (String).

**Annotation:** `@JsonProperty("place_id")` on `placeId`. The `key` field carries the API key in the body for the update endpoint (unlike Add/Get/Delete which use a query parameter).

---

### 2.5 `main/java/org/api/automation/models/response`

All response POJOs carry `@JsonIgnoreProperties(ignoreUnknown = true)`. Without it, the day the backend adds a new field, every test that deserializes that response breaks with `UnrecognizedPropertyException`. Request models are strict; response models tolerate additions.

#### `AddBookResponse.java`

**Fields:** `message` (String), `id` (String).

**Annotations:**
- `@JsonProperty("Msg")` on `message` — the primary name in the API response.
- `@JsonAlias({"msg", "message"})` on `message` — accepts alternative casings without failing.
- `@JsonProperty("ID")` on `id` — the API returns the book id with an uppercase key.
- `@JsonAlias({"id"})` on `id` — tolerates a lowercase alternative.

---

#### `AddPlaceResponse.java`

**Fields:** `status` (String), `placeId` (String), `scope` (String), `reference` (String), `id` (String).

**Annotation:** `@JsonProperty("place_id")` on `placeId`.

---

#### `AddProductResponse.java`

**Fields:** `message` (String), `productId` (String).

No Jackson annotations — both field names match the API.

---

#### `Course.java`

**Fields:** `courseTitle` (String), `price` (String).

**Why `price` is a String, not int:** The API returns prices as quoted strings (`"price": "50"`). Modelling as `int` causes Jackson to coerce, which breaks on `"50.00"` or `""`. The wire format is matched, and `priceAsInt()` converts when arithmetic is needed.

**Convenience method:** `priceAsInt()` parses `price` as an integer. Used in assertion and sum calculations.

**Replaces three old POJOs:** The original framework had `WebAutomation`, `API`, and `Mobile` classes each with identical `courseTitle`/`price` fields. Jackson binds by shape, not by key name, so one `Course` class serves all three arrays.

---

#### `CourseCatalog.java`

**Fields:** `webAutomation` (List\<Course\>), `api` (List\<Course\>), `mobile` (List\<Course\>).

**Convenience method:** `all()` returns a combined list of all courses across the three disciplines, for catalog-wide assertions (e.g. checking total count).

---

#### `CourseDetailsResponse.java`

**Fields:** `instructor` (String), `url` (String), `services` (String), `expertise` (String), `linkedIn` (String), `courses` (CourseCatalog).

**Role:** Three-level nesting: this object → `CourseCatalog` → `List<Course>`. REST Assured builds the whole tree from a single `.as(CourseDetailsResponse.class)` call.

**Convenience methods:**
- `findCourseByTitle(String title)` — searches all three discipline lists and returns `Optional<Course>`. Returns `Optional.empty()` rather than `null` so a missing course produces an explicit assertion failure rather than a `NullPointerException`.
- `webAutomationTitles()` — streams the `webAutomation` list and collects titles.

---

#### `CreateOrderResponse.java`

**Fields:** `message` (String), `orders` (List\<String\>), `productOrderId` (List\<String\>).

Both `orders` and `productOrderId` are lists because the endpoint accepts a batch. The old code accessed them with `js.getString("orders[0]")` — index arithmetic in a string. Typed as lists, the same value is `getOrders().get(0)` and the list size can be asserted.

**Convenience method:** `firstOrderId()` returns `orders.get(0)` or `null`.

---

#### `GeoLocation.java`

**Fields:** `latitude` (double), `longitude` (double).

**Why this is not the same as request's `Location`:** The Places API accepts `lat`/`lng` but returns `latitude`/`longitude`. Using one POJO for both directions fails at deserialization with `UnrecognizedPropertyException`. Separate models let each describe the wire format honestly.

---

#### `GetPlaceResponse.java`

**Fields:** `location` (GeoLocation), `accuracy` (int), `name` (String), `phoneNumber` (String), `address` (String), `types` (String), `website` (String), `language` (String).

**Annotations:** `@JsonProperty("phone_number")` on `phoneNumber`.

**Why `types` is a String, not List:** Add Place accepts `"types": ["shoe park", "shop"]` (a JSON array), but Get Place returns `"types": "shoe park,shop"` (a comma-separated string). Declaring it as `List<String>` produces `MismatchedInputException`. The model matches the wire format; `typesList()` splits for comparison.

**Convenience method:** `typesList()` splits the comma-delimited `types` string into a `List<String>`.

---

#### `GraphQLResponse.java`

**Fields:** `data` (Map\<String, Object\>), `errors` (List\<GraphQLError\>).

**Role:** The GraphQL response envelope — the most important response model in the framework. Every GraphQL response arrives in this shape regardless of what was queried, because the URL never changes and what varies is the query document.

**Critical behaviour:** A GraphQL server returns HTTP 200 even when an operation fails. The `errors` array is the authoritative indicator of failure. Status code 200 proves only that the HTTP transport worked.

**Methods:**
- `hasErrors()` — returns `true` when `errors` is non-null and non-empty.
- `errorMessages()` — joins all error `message` fields with `"; "` for use in assertion failure messages.

**Inner class `GraphQLError`:** Fields are `message` (String), `locations` (List\<Map\<String, Object\>\>), `path` (List\<Object\>). Also carries `@JsonIgnoreProperties(ignoreUnknown = true)`.

---

#### `LoginResponse.java`

**Fields:** `token` (String), `userId` (String), `message` (String).

**Security note:** `toString()` masks the token with `****`.

---

#### `MessageResponse.java`

**Fields:** `message` (String), `status` (String).

**Role:** Reusable acknowledgement model for every endpoint that returns only a message. Covers Update Place, Delete Place, Delete Book, Delete Product, and Delete Order — five different endpoints that differ only in what they call the message field.

**Annotation:**
- `@JsonProperty("message")` on `message` — sets the serialization name.
- `@JsonAlias({"msg", "Msg", "MSG"})` — accepts all the casings the practice APIs use. `@JsonAlias` applies only to deserialization (input); `@JsonProperty` controls serialization (output). Together they make one class tolerate multiple inconsistent APIs.

---

#### `OAuthTokenResponse.java`

**Fields:** `accessToken` (String), `tokenType` (String), `expiresIn` (Long), `refreshToken` (String), `scope` (String).

**Annotations (all `@JsonProperty`):** `access_token` → `accessToken`, `token_type` → `tokenType`, `expires_in` → `expiresIn`, `refresh_token` → `refreshToken`. These names are fixed by RFC 6749 section 5.1, which is why they are snake_case.

**Security note:** `toString()` masks the access token.

---

### 2.6 `main/java/org/api/automation/utils`

#### `JsonUtils.java`

**Full path:** `src/main/java/org/api/automation/utils/JsonUtils.java`

**Role:** Typed wrappers around `JsonPath` extraction and a shared `ObjectMapper`. Eliminates repeated `new JsonPath(response.asString())` calls scattered across test files.

**Shared field:** `MAPPER` — a single `ObjectMapper` instance with `INDENT_OUTPUT` enabled, used for all serialize/deserialize calls.

**Methods:**

| Method | Parameters | Returns | Notes |
|---|---|---|---|
| `toJsonPath(String rawJson)` | A raw JSON string | `JsonPath` | For JSON not from an HTTP call (e.g. a file read). |
| `toJsonPath(Response response)` | REST Assured Response | `JsonPath` | For repeated extraction from one response. |
| `getString(Response, String jsonPath)` | Response + GPath expression | `String` | Single-call extraction. |
| `getInt(Response, String jsonPath)` | Response + GPath expression | `int` | Single-call extraction. |
| `getList(Response, String jsonPath)` | Response + GPath expression | `List<T>` | Single-call extraction. |
| `size(Response, String jsonPath)` | Response + GPath expression | `int` | Appends `.size()` to the path for array size queries. |
| `toJson(Object)` | Any object | `String` | Jackson serialization; wraps `JsonProcessingException`. |
| `fromJson(String, Class<T>)` | JSON string + target type | `T` | Jackson deserialization; wraps `JsonProcessingException`. |

**Used by:** `SerializationTest`, `JsonPathParsingTest`, `PlaceStepDefinitions`.

---

#### `ResourceReader.java`

**Full path:** `src/main/java/org/api/automation/utils/ResourceReader.java`

**Role:** Reads static test resources from the classpath rather than from relative file paths. Classpath-relative paths work under any working directory — IDE, Jenkins, packaged jar.

**Methods:**

| Method | Parameters | Returns | Notes |
|---|---|---|---|
| `readAsString(resourcePath)` | Classpath path (e.g. `"payloads/add-place.json"`) | `String` | Reads as UTF-8. Throws `RuntimeException` if not found. |
| `readJsonTemplate(resourcePath, replacements...)` | Path + alternating key/value pairs | `String` | Reads the file, then replaces every `{{key}}` with the corresponding value. Throws if replacement count is not even. |
| `asFile(resourcePath)` | Classpath path | `java.io.File` | Resolves to a `File` on disk via `getResource()`. Required for REST Assured's `.multiPart()` which needs a real file. |

**Template syntax:** `{{placeholder}}` — double curly braces. Example: `"isbn": "{{isbn}}"` is replaced by `"isbn": "abc123"` when called with `readJsonTemplate("payloads/add-book.json", "isbn", "abc123", ...)`.

**Used by:** `BookPayloadTest`, `SerializationTest`, `JsonPathParsingTest`, `EcomOrderE2ETest`.

---

### 2.7 `test/java/org/api/automation/base`

#### `BaseTest.java`

**Full path:** `src/test/java/org/api/automation/base/BaseTest.java`

**Role:** Abstract base class that every test class extends. Provides one spec builder per API system and a shared traffic log stream. Returning a new `RequestSpecification` per call is the key design decision: there is no shared mutable spec that parallel tests could corrupt.

**Annotation:** `@Listeners(TestListener.class)` — registers the `TestListener` on every subclass without requiring each suite XML to declare it (though the suite XMLs also declare it for completeness).

**Shared state:**

| Field | Type | Purpose |
|---|---|---|
| `log` | `Logger` | Per-class logger (uses `getClass()` so each subclass gets its own logger name). |
| `TRAFFIC_LOG` | `static PrintStream` | Opened once, shared across all tests. Writes to `target/logs/api-traffic.log`. Thread-safe (synchronised `PrintStream`). Falls back to `System.out` if the file cannot be created. |

**`@BeforeSuite logEnvironment()`:** Logs the active environment name (`qa`, `staging`, etc.) at the start of every suite run. Useful for debugging CI failures that used the wrong environment.

**Spec builder methods:**

| Method | Base URI | Key headers / params | Notes |
|---|---|---|---|
| `placesSpec()` | `places.baseUrl` | `?key=places.apiKey`, `Content-Type: application/json` | Used by all Places tests and BDD steps. |
| `librarySpec()` | `library.baseUrl` | `Content-Type: application/json` | Used by Book tests. |
| `ecomSpec()` | `ecom.baseUrl` | `Content-Type: application/json` | Unauthenticated — used only for login. |
| `ecomSpec(String token)` | `ecom.baseUrl` | + `Authorization: <token>` header | Authenticated variant for all post-login calls. |
| `graphqlSpec()` | `graphql.baseUrl` | `Content-Type: application/json` | Used by GraphQL tests. |

All specs attach `RequestLoggingFilter` and `ResponseLoggingFilter` targeting `TRAFFIC_LOG`.

**`trafficLog()`:** Static accessor so non-BaseTest classes (specifically `Hooks` and `PlaceStepDefinitions`) can attach the same filters to their own specs.

---

### 2.8 `test/java/org/api/automation/listeners`

#### `TestListener.java`

**Full path:** `src/test/java/org/api/automation/listeners/TestListener.java`

**Role:** Implements TestNG's `ITestListener` to log the lifecycle of every test. Registered via `@Listeners` in `BaseTest` and also listed explicitly in each suite XML.

**Callbacks and what they log:**

| Callback | Log level | Content |
|---|---|---|
| `onTestStart` | INFO | `=== START ClassName.methodName ===` |
| `onTestSuccess` | INFO | `=== PASS ClassName.methodName (Nms) ===` |
| `onTestFailure` | ERROR | `=== FAIL ClassName.methodName (Nms) ===` + the failure message + a pointer to `api-traffic.log` |
| `onTestSkipped` | WARN | `=== SKIP ClassName.methodName ===` + skip reason |
| `onFinish` | INFO | Suite name, count of passed/failed/skipped |

**Used by:** All test classes (via `BaseTest`). Also listed in all four suite XMLs.

---

### 2.9 `test/java/org/api/automation/bdd`

#### `ScenarioContext.java`

**Full path:** `src/test/java/org/api/automation/bdd/context/ScenarioContext.java`

**Role:** A per-scenario state container managed by PicoContainer. Any step-definition or hook class that declares `ScenarioContext` as a constructor parameter receives **the same instance** for the duration of one scenario, and a **fresh one** for the next.

**Problem it replaces:** The original `StepDefinition` used `static String placeId`, which leaked between scenarios and made parallel execution impossible. A static field keeps its value after a scenario ends, so a Delete scenario would operate on whatever id the previous Add scenario left behind.

**Fields:**

| Field | Type | Purpose |
|---|---|---|
| `request` | `RequestSpecification` | The spec assembled by Given steps. Read by the When step to fire the call. |
| `response` | `Response` | The response from the most recent When step. Read by Then/And steps. |
| `values` | `Map<String, Object>` | Typed free-form carry-forward values (e.g. `"placeId"`, `"submittedName"`). |

**Methods:**

| Method | Behaviour |
|---|---|
| `getRequest()` | Returns `request`; throws `IllegalStateException` with a useful message if no Given step has run. |
| `setRequest(RequestSpecification)` | Called by Given steps. |
| `getResponse()` | Returns `response`; throws `IllegalStateException` with a useful message if no When step has run. |
| `setResponse(Response)` | Called by the When step. |
| `put(String key, Object value)` | Stores a value in `values`. |
| `get(String key, Class<T> type)` | Retrieves and type-casts a value; throws `IllegalStateException` naming available keys if missing. |
| `getString(String key)` | `get(key, String.class)`. |
| `has(String key)` | Returns `true` if the key is present. |

---

#### `CucumberTestRunner.java`

**Full path:** `src/test/java/org/api/automation/bdd/runner/CucumberTestRunner.java`

**Role:** Runs the Cucumber feature files under the TestNG framework. Extends `AbstractTestNGCucumberTests`, which exposes each scenario as a TestNG test. This means one test provider (TestNG), one `testng.xml`, and one report.

**`@CucumberOptions` settings:**

| Option | Value | Why |
|---|---|---|
| `features` | `src/test/resources/features` | The directory Cucumber scans for `.feature` files. |
| `glue` | `org.api.automation.bdd.stepdefinitions` | The package Cucumber scans for step definitions and hooks. |
| `plugin` | `pretty`, `json:target/json-reports/cucumber-report.json`, `html:target/cucumber-reports/cucumber.html`, `summary` | Pretty = readable console. JSON feeds maven-cucumber-reporting (the HTML Jenkins publishes). HTML = self-contained browser report. Summary = pass/fail count at the end. |
| `monochrome` | `true` | Removes ANSI colour codes for clean CI logs. |

**Tag filtering:** The `tags` option is not hardcoded. It reads from the `cucumber.filter.tags` system property, set by Maven Surefire from the `<cucumber.filter.tags>` POM property. This allows Jenkins to parameterize the filter without a code change:
```
mvn test -Dcucumber.filter.tags="@AddPlace"
```

**`scenarios()` override:** Overrides the inherited `@DataProvider` with `parallel = true`. This enables scenario-level parallelism. It is safe because `ScenarioContext` is per-scenario (PicoContainer creates a fresh instance) and `BaseTest` spec builders return a fresh `RequestSpecification` per call — no shared mutable state.

---

#### `Hooks.java`

**Full path:** `src/test/java/org/api/automation/bdd/stepdefinitions/Hooks.java`

**Role:** Cucumber lifecycle hooks. PicoContainer injects `ScenarioContext`, so state set here is visible to every step in the same scenario.

**Hooks defined:**

| Annotation | Tag filter | Order | What it does |
|---|---|---|---|
| `@Before(order = 0)` | None (runs for every scenario) | 0 (first) | Logs `=== SCENARIO START: <name> ===`. |
| `@Before(value = "@DeletePlace", order = 10)` | `@DeletePlace` only | 10 (after logging) | Creates a fresh place via the Places API and stores its `place_id` in `context.put("placeId", ...)`. Each run gets its own place; no cross-scenario dependency. |
| `@After(order = 0)` | None (runs for every scenario) | 0 | Logs `=== SCENARIO FAILED ===` or `=== SCENARIO PASSED ===`. |

**Why the `@DeletePlace` hook creates a new place unconditionally:** The old null-check meant the second run reused the first run's id. Running unconditionally ensures isolation: each execution of the Delete scenario operates on a place it created itself.

---

#### `PlaceStepDefinitions.java`

**Full path:** `src/test/java/org/api/automation/bdd/stepdefinitions/PlaceStepDefinitions.java`

**Role:** Step definitions for the Places feature. PicoContainer injects `ScenarioContext` — no static fields, no inheritance from BaseTest.

**Step methods:**

| Gherkin step | Method | What it does |
|---|---|---|
| `Given Add Place Payload with {string} {string} {string}` | `addPlacePayloadWith(name, language, address)` | Calls `TestDataFactory.addPlace()`, sets the result as the body on a fresh `placesSpec()`, and stores the submitted name and address in context. |
| `Given Delete Place Payload with the created place_Id` | `deletePlacePayloadWithCreatedPlaceId()` | Reads `"placeId"` from context (stored by the `@DeletePlace` hook), builds a `DeletePlaceRequest`, sets it as the body. |
| `When User calls {string} API with {string} http request` | `userCallsApiWithHttpRequest(endpoint, method)` | Resolves the endpoint via `Paths.fromName()`, retrieves the spec from context, fires the appropriate HTTP verb, and stores the response in context. |
| `Then API call is successful with status code {int}` | `apiCallIsSuccessfulWithStatusCode(int)` | `assertEquals` on `context.getResponse().getStatusCode()`. |
| `And {string} in response body is {string}` | `keyInResponseBodyIs(jsonPath, expectedValue)` | Extracts the value at the JsonPath expression from the response and asserts equality. |
| `Then place_Id is captured from the response` | `placeIdIsCapturedFromTheResponse()` | Extracts `place_id` from the response and stores it in context as `"placeId"`. |
| `Then the created place can be retrieved with name {string}` | `theCreatedPlaceCanBeRetrievedWithName(expectedName)` | Assembles a GET request with `?place_id=<id>`, calls the API (via the When step method), asserts status 200, asserts the returned `name` equals `expectedName`. |
| `And the created place has address {string}` | `theCreatedPlaceHasAddress(expectedAddress)` | Asserts the `address` field in the currently set response. |

---

### 2.10 `test/java/org/api/automation/tests`

_(Detailed test-by-test coverage is in Section 3.)_

The test classes are organized into sub-packages by domain:

| Package | Class | Domain |
|---|---|---|
| `tests.auth` | `OAuthTest` | OAuth 2.0 token flow |
| `tests.ecom` | `EcomOrderE2ETest` | E-commerce end-to-end |
| `tests.graphql` | `GraphQLTest` | GraphQL introspection |
| `tests.json` | `JsonPathParsingTest` | Complex JsonPath parsing |
| `tests.library` | `BookPayloadTest` | Library API, payload styles |
| `tests.library` | `ExcelDataDrivenTest` | Library API, Excel-driven |
| `tests.place` | `AddPlaceDataDrivenTest` | Places API, parameterised |
| `tests.place` | `AddPlaceTest` | Places API, assertion styles |
| `tests.place` | `PlaceCrudE2ETest` | Places CRUD lifecycle |
| `tests.serialization` | `SerializationTest` | Jackson mapping, offline |

---

## 3. Test Execution Flows

---

### 3.1 `AddPlaceTest`

**File:** `src/test/java/org/api/automation/tests/place/AddPlaceTest.java`

**Extends:** `BaseTest`

**Groups:** `smoke`, `regression`, `negative`, `needs-verification`

#### `shouldAddPlaceAndReturnExpectedFields`

| Attribute | Detail |
|---|---|
| Intent | Demonstrates the fluent (Hamcrest chained) assertion style and validates the main Add Place happy path in one call. |
| Base URI | `places.baseUrl` (from `BaseTest.placesSpec()`) |
| Data source | `TestDataFactory.addPlace("Frontline House", "English", "123 Main St")` |
| Endpoint | `POST /maps/api/place/add/json` |
| Request POJO | `AddPlaceRequest` (serialized by Jackson) |
| Response POJO | None — assertions are chained on the REST Assured response directly |
| Assertions | Status code 200; `Content-Type: application/json`; `status == "OK"`; `scope == "APP"`; `place_id` is not blank; response time under 10 seconds |
| Groups | `smoke`, `regression` |

The response-time assertion (`lessThan(10_000L)`) is an SLA check — it catches a degraded environment even when the API returns correct data.

#### `shouldReturnUsablePlaceIdInTypedResponse`

| Attribute | Detail |
|---|---|
| Intent | Same Add Place call, validated through a typed `AddPlaceResponse` (extract-then-assert style). Demonstrates that `getPlaceId()` is reusable. |
| Base URI | `places.baseUrl` |
| Data source | `TestDataFactory.defaultPlace()` |
| Endpoint | `POST /maps/api/place/add/json` |
| Request POJO | `AddPlaceRequest` |
| Response POJO | `AddPlaceResponse` — deserialized via `.as(AddPlaceResponse.class)` |
| Assertions | Status code 200 (in the fluent chain); `status == "OK"`; `scope == "APP"`; `placeId` not null |
| Groups | `smoke`, `regression` |

#### `shouldReturnJsonContentTypeHeader`

| Attribute | Detail |
|---|---|
| Intent | Asserts on the `Content-Type` response header. Uses `containsString` not `equalTo` so that charset suffixes (`; charset=UTF-8`) do not break the assertion. |
| Endpoint | `POST /maps/api/place/add/json` |
| Assertions | `Content-Type` header contains `"application/json"` |
| Groups | `regression` |

#### `shouldRejectRequestWithMissingRequiredFields`

| Attribute | Detail |
|---|---|
| Intent | Negative test — confirms that an empty payload receives a 4xx. Tagged `needs-verification` and excluded from the default suite because the expected status code has not been confirmed against the live API. |
| Data source | `new AddPlaceRequest()` — all fields null/default |
| Endpoint | `POST /maps/api/place/add/json` |
| Assertions | Status code is in the range 400–499 |
| Groups | `negative`, `needs-verification` |

---

### 3.2 `PlaceCrudE2ETest`

**File:** `src/test/java/org/api/automation/tests/place/PlaceCrudE2ETest.java`

**Extends:** `BaseTest`

**Class-level `@Test` groups:** `e2e`, `regression`

**`dependsOnMethods` chain:** `createPlace` → `createdPlaceShouldBeRetrievableWithSubmittedValues` → `updateShouldChangeAddress` → `updatedAddressShouldPersist` → `deletePlace`

**Instance field:** `private String placeId` — carries the created place id across all five steps. This is an instance field (not static), so parallel class execution cannot corrupt it.

#### `createPlace`

| Attribute | Detail |
|---|---|
| Intent | POST a new place and capture its id for use by subsequent steps. |
| Data source | `TestDataFactory.addPlace("Frontline House", "English", "29, side layout, cohen 09")` |
| Endpoint | `POST /maps/api/place/add/json` |
| Request POJO | `AddPlaceRequest` |
| Response POJO | `AddPlaceResponse` |
| Assertions | `status == "OK"`; `placeId` not null |
| Side effect | Sets `this.placeId` |

#### `createdPlaceShouldBeRetrievableWithSubmittedValues`

| Attribute | Detail |
|---|---|
| Intent | Verify the just-created place is retrievable and contains the values submitted. |
| Endpoint | `GET /maps/api/place/get/json?place_id=<placeId>` |
| Response POJO | `GetPlaceResponse` |
| Assertions | `name == "Frontline House"`; `address == "29, side layout, cohen 09"` |
| `dependsOnMethods` | `createPlace` |

#### `updateShouldChangeAddress`

| Attribute | Detail |
|---|---|
| Intent | PUT an address change and confirm the acknowledgement message. |
| Endpoint | `PUT /maps/api/place/update/json` |
| Request POJO | `UpdatePlaceRequest` — constructed with `placeId`, new address `"70 Summer walk, USA"`, and the API key from config |
| Response POJO | `MessageResponse` |
| Assertions | `message == "Address successfully updated"` |
| `dependsOnMethods` | `createdPlaceShouldBeRetrievableWithSubmittedValues` |

#### `updatedAddressShouldPersist`

| Attribute | Detail |
|---|---|
| Intent | GET the place again and confirm the address reflects the update, not the original value. This is the assertion that closes the loop — without it the update could be accepted but not stored. |
| Endpoint | `GET /maps/api/place/get/json?place_id=<placeId>` |
| Response POJO | `GetPlaceResponse` |
| Assertions | `address == "70 Summer walk, USA"` |
| `dependsOnMethods` | `updateShouldChangeAddress` |

#### `deletePlace`

| Attribute | Detail |
|---|---|
| Intent | Clean up the test data by deleting the created place. `alwaysRun = true` ensures this runs even when an upstream step failed. |
| Endpoint | `DELETE /maps/api/place/delete/json` |
| Request POJO | `DeletePlaceRequest` |
| Response POJO | `MessageResponse` |
| Assertions | `status == "OK"` |
| `dependsOnMethods` | `updatedAddressShouldPersist` |
| `alwaysRun` | `true` |

---

### 3.3 `AddPlaceDataDrivenTest`

**File:** `src/test/java/org/api/automation/tests/place/AddPlaceDataDrivenTest.java`

**Extends:** `BaseTest`

#### `shouldCreateAndRetrievePlace` (DataProvider: `places`)

| Attribute | Detail |
|---|---|
| Intent | Parameterised happy-path: create a place, verify the returned `placeId`, GET it back and confirm name and address round-trip correctly. Runs 3 times (one per row). |
| Data source | `@DataProvider(name = "places")` — inline `Object[][]`: Frontline House/English/123 Main St, Backline House/Spanish/456 Elm St, Sidewalk House/French/789 Oak St |
| Endpoint 1 | `POST /maps/api/place/add/json` |
| Request POJO | `AddPlaceRequest` |
| Response POJO | `AddPlaceResponse` |
| Assertions after POST | `status == "OK"`; `placeId` not null |
| Endpoint 2 | `GET /maps/api/place/get/json?place_id=<placeId>` |
| Response POJO 2 | `GetPlaceResponse` |
| Assertions after GET | `name` equals the submitted name; `address` equals the submitted address |
| Cleanup | `DELETE /maps/api/place/delete/json` (no assertion) |
| Groups | `regression`, `datadriven` |

#### `shouldCreatePlaceWithGeneratedData` (DataProvider: `generatedPlaces`)

| Attribute | Detail |
|---|---|
| Intent | Shows that data providers can be dynamic. Generates 2 unique place names at runtime to avoid duplicate-name rejections. |
| Data source | `@DataProvider(name = "generatedPlaces")` — builds `"Generated Place <uniqueSuffix>"` names at provider execution time |
| Endpoint | `POST /maps/api/place/add/json` |
| Assertions | `status == "OK"` |
| Cleanup | `DELETE /maps/api/place/delete/json` |
| Groups | `regression`, `datadriven` |

---

### 3.4 `BookPayloadTest`

**File:** `src/test/java/org/api/automation/tests/library/BookPayloadTest.java`

**Extends:** `BaseTest`

All four tests hit `POST /Library/Addbook.php` using `librarySpec()`, then clean up with `POST /Library/DeleteBook.php`.

#### `shouldAddBookFromPojoPayload`

| Attribute | Detail |
|---|---|
| Intent | Method 1 of 4 payload styles: POJO serialized by Jackson. Compile-time safety and refactorability. |
| Data source | `TestDataFactory.uniqueBook("2645")` |
| Request POJO | `AddBookRequest` |
| Response POJO | `AddBookResponse` |
| Assertions | `id` not null; `id == request.expectedBookId()` (i.e. `isbn + "2645"`) |
| Groups | `regression`, `external-api` |

#### `shouldAddBookFromJsonFileTemplate`

| Attribute | Detail |
|---|---|
| Intent | Method 2: a `.json` file with `{{placeholder}}` substitution. The file stays human-readable; values are injected per test. |
| Data source | `ResourceReader.readJsonTemplate("payloads/add-book.json", "isbn", generatedIsbn, "aisle", "2645")` |
| Response | Extracted via `response.jsonPath().getString("ID")` (raw JsonPath, no POJO) |
| Assertions | `bookId == isbn + "2645"` |
| Groups | `regression`, `external-api` |

#### `shouldAddBookFromMapPayload`

| Attribute | Detail |
|---|---|
| Intent | Method 3: a `Map<String, Object>` body — REST Assured serializes it to JSON automatically. Nested map values become nested JSON objects. Demonstrates how spreadsheet rows (which are already key-value pairs) map naturally. |
| Data source | Inline `LinkedHashMap` construction |
| Assertions | `bookId == isbn + "2645"` |
| Groups | `regression`, `external-api` |

#### `shouldAddThenDeleteBook` (DataProvider: `aisles`)

| Attribute | Detail |
|---|---|
| Intent | Method 4: data-driven add-and-delete. ISBNs are generated per row so the test is repeatable. Runs 3 times (aisles: 12345, 54321, 67890). |
| Data source | `@DataProvider(name = "aisles")` — inline `Object[][]` |
| Request POJO | `AddBookRequest` from `TestDataFactory.uniqueBook(aisle)` |
| Response POJO | `AddBookResponse` |
| Assertions | `id` not null per aisle; status 200 on delete |
| Groups | `regression`, `datadriven`, `external-api` |

---

### 3.5 `ExcelDataDrivenTest`

**File:** `src/test/java/org/api/automation/tests/library/ExcelDataDrivenTest.java`

**Extends:** `BaseTest`

**Workbook:** `testdata/TestData.xlsx`, sheet `TestData`, key column `TestCases`.

#### `shouldAddBookUsingSpreadsheetData`

| Attribute | Detail |
|---|---|
| Intent | End-to-end: read book data from a spreadsheet row, add the book, assert on the returned id, delete it. Confirms that `NumberToTextConverter` prevents `"227.0"` from appearing in the aisle field. |
| Data source | `ExcelDataReader.getRow(..., "AddBook")` — Data1=book name, Data2=author, Data3=isbn base, Data4=aisle |
| Endpoint | `POST /Library/Addbook.php` |
| Assertions | `bookId == isbn + aisle`; intermediate assertion that `Data4 == "227"` (not `"227.0"`) |
| Groups | `regression`, `datadriven`, `external-api` |

#### `shouldReadRowsByKeyNotByPosition`

| Attribute | Detail |
|---|---|
| Intent | Offline validation that `ExcelDataReader` returns the correct row by key and that different keys return different rows. No network call. |
| Data source | `ExcelDataReader.getRow(..., "AddBook")` and `ExcelDataReader.getRow(..., "Login")` |
| Assertions | `addBook.get("TestCases") == "AddBook"`; `addBook.get("Data1") == "Appium"`; `login.get("Data1") == "abc"` |
| Groups | `offline`, `regression` |

#### `shouldFailClearlyWhenTestCaseRowIsMissing`

| Attribute | Detail |
|---|---|
| Intent | Confirms that a missing row throws `IllegalArgumentException` (not `IndexOutOfBoundsException` or null). `expectedExceptions` makes TestNG treat the throw as a pass. |
| Assertion style | `expectedExceptions = IllegalArgumentException.class` |
| Groups | `offline` |

---

### 3.6 `OAuthTest`

**File:** `src/test/java/org/api/automation/tests/auth/OAuthTest.java`

**Extends:** `BaseTest`

**Token caching:** `private String cachedToken` + `private String token()`. The first call to `token()` fetches a new token from the OAuth endpoint and caches it in the field. Subsequent calls reuse the cached value — one network round-trip per test class instance, not one per test method.

**TestNG metadata:** `@BeforeClass` is not used; token is fetched lazily. All credential-dependent tests are in group `requires-credentials` and excluded from the default suite.

#### `shouldObtainAccessTokenWithClientCredentials`

| Attribute | Detail |
|---|---|
| Intent | Directly test the client-credentials grant: send id + secret, receive an access token and a lifetime. |
| Base URI | `oauth.baseUrl` |
| Request | `Content-Type: application/x-www-form-urlencoded` form params: `client_id`, `client_secret`, `grant_type=client_credentials`, `scope` |
| Endpoint | `POST /oauthapi/oauth2/resourceOwner/token` |
| Response POJO | `OAuthTokenResponse` |
| Assertions | `accessToken` not blank; `expiresIn` not null; `expiresIn > 0` |
| Groups | `auth`, `requires-credentials` |

#### `shouldAccessProtectedResourceWithToken`

| Attribute | Detail |
|---|---|
| Intent | Use the cached token as a query parameter to access a protected GET endpoint, then deserialize the nested response tree. |
| Endpoint | `GET /oauthapi/getCourseDetails?access_token=<token>` |
| Response POJO | `CourseDetailsResponse` (contains `CourseCatalog` which contains `List<Course>`) |
| Assertions | `instructor == "RahulShetty"`; `linkedIn` not blank; `courses` not null; web automation catalogue contains `"Selenium Webdriver Java"` |
| Groups | `auth`, `requires-credentials` |

#### `shouldFindSpecificCourseInProtectedResource`

| Attribute | Detail |
|---|---|
| Intent | Demonstrates `findCourseByTitle()` — a stream filter over the typed object graph vs. a JsonPath string lookup. |
| Endpoint | `GET /oauthapi/getCourseDetails?access_token=<token>` |
| Response POJO | `CourseDetailsResponse` |
| Assertions | `findCourseByTitle("SoapUI Webservices testing")` is present; price is `"40"` |
| Groups | `auth`, `requires-credentials` |

#### `shouldExtractAuthorizationCodeFromRedirectUrl`

| Attribute | Detail |
|---|---|
| Intent | Offline test. Parses the `code` parameter out of an OAuth redirect URL using proper query-string parsing (not `split("code=")[1].split("&scope")[0]`, which breaks when parameters are reordered). |
| Data source | Hardcoded redirect URL string |
| Assertions | Exact code value (still percent-encoded) |
| Groups | `auth`, `offline` |

#### `shouldExtractCodeRegardlessOfParameterOrder`

| Attribute | Detail |
|---|---|
| Intent | Offline test. Verifies that `extractAuthorizationCode()` works when `code` is the first, middle, or last parameter, and throws `IllegalArgumentException` when `code` is absent. |
| Data source | Inline URL strings |
| Assertions | Correct code extracted from three orderings; `assertThrows` for missing-code case |
| Groups | `auth`, `offline` |

---

### 3.7 `EcomOrderE2ETest`

**File:** `src/test/java/org/api/automation/tests/ecom/EcomOrderE2ETest.java`

**Extends:** `BaseTest`

**`@BeforeClass login()`:** Authenticates and stores `token` and `userId`. `alwaysRun = true` means it runs even in suites filtered by group.

**`@AfterClass cleanUp()`:** Deletes the order and product in reverse creation order. `alwaysRun = true` ensures cleanup runs even when a test fails. Failures inside cleanup are caught and logged rather than thrown (to preserve the original failure as the suite result).

#### `shouldAddProductWithImageUpload`

| Attribute | Detail |
|---|---|
| Intent | Add a product via multipart form upload including a binary image file. Demonstrates `multiPart()` instead of `body()`. |
| Base URI | `ecom.baseUrl` |
| Auth | `Authorization: <token>` header via `ecomSpec(token)` |
| Content-Type | `multipart/form-data` |
| Data source | Inline params + `ResourceReader.asFile("testdata/laptop.jpg")` for the image file |
| Endpoint | `POST /api/ecom/product/add-product` |
| Response POJO | `AddProductResponse` |
| Assertions | `productId` not blank |
| Side effect | Sets `this.productId` |
| Groups | `e2e`, `regression`, `requires-credentials` |

#### `shouldCreateOrderForProduct`

| Attribute | Detail |
|---|---|
| Intent | Place an order for the product just created. Confirms the order acknowledgement and that the response references the correct product. |
| Auth | `Authorization: <token>` |
| Data source | `CreateOrderRequest.forSingleProduct("India", productId)` |
| Endpoint | `POST /api/ecom/order/create-order` |
| Request POJO | `CreateOrderRequest` (wraps a `List<OrderDetail>`) |
| Response POJO | `CreateOrderResponse` |
| Assertions | `message == "Order Placed Successfully"`; `firstOrderId()` not null; `productOrderId` contains `productId` |
| Side effect | Sets `this.orderId` |
| `dependsOnMethods` | `shouldAddProductWithImageUpload` |
| Groups | `e2e`, `regression`, `requires-credentials` |

---

### 3.8 `GraphQLTest`

**File:** `src/test/java/org/api/automation/tests/graphql/GraphQLTest.java`

**Extends:** `BaseTest`

All tests POST to `POST /gq/graphql` via `graphqlSpec()`. The private helpers `query(String)` and `query(String, Map)` assert no errors; `rawQuery(GraphQLRequest)` returns the raw response for error-case tests.

#### `shouldExecuteMinimalQuery`

| Intent | Connectivity check using the smallest valid document (`SCHEMA_QUERY_TYPE`). Asserts no errors, `data` not null, root query type name not blank. |
| Groups | `graphql`, `smoke` |

#### `shouldDiscoverAvailableQueryFields`

| Intent | Lists every root query field exposed by the server. No assertions on the specific fields — the test passes as long as at least one field exists. Useful as an exploration tool. |
| Groups | `graphql` |

#### `shouldExecuteQueryWithVariables`

| Intent | Sends `TYPE_INFO_BY_NAME` with `{"typeName": "String"}` in the variables map. Asserts that the returned `__type.name` is `"String"` and `__type.kind` is `"SCALAR"`. Demonstrates that variables are substituted server-side, keeping the document text constant. |
| Groups | `graphql`, `regression` |

#### `variableValueShouldDetermineTheResult`

| Intent | Runs the same query twice with different variable values (`"String"` and `"Boolean"`) and asserts that each returns the correct type name. Proves the variable is what changes the result. |
| Groups | `graphql`, `regression` |

#### `shouldReportErrorWhenMandatoryVariableIsMissing`

| Intent | Sends `TYPE_INFO_BY_NAME` with an empty variables map, omitting the required `$typeName`. Asserts `hasErrors()` is true — the server must report a variable-missing error, not silently succeed. |
| Groups | `graphql`, `regression`, `negative` |

#### `shouldReportErrorsInEnvelopeForInvalidField`

| Intent | The central GraphQL lesson test: sends `INVALID_FIELD` (a field no schema has), gets the raw response, asserts `hasErrors()` regardless of HTTP status, and asserts `data` is null. Tests that pass on status code 200 alone would wrongly pass here. |
| Groups | `graphql`, `smoke`, `negative` |

#### `validQueryShouldReturnDataAndNoErrors`

| Intent | Counterpart of the above: a valid query must return HTTP 200, `data` not null, and no errors. |
| Groups | `graphql`, `smoke` |

#### `shouldReportErrorForMalformedDocument`

| Intent | Sends `MALFORMED_DOCUMENT` (unclosed brace). Asserts `hasErrors()` is true. A syntax error is rejected at parse time; the status code varies by server. |
| Groups | `graphql`, `negative` |

#### `shouldReportWhetherEndpointSupportsMutations`

| Intent | Exploratory test. Sends `DISCOVER_MUTATION_FIELDS` and logs whether the endpoint declares a mutation type. Passes unconditionally — the finding is the log output, not a binary assertion. |
| Groups | `graphql` |

---

### 3.9 `JsonPathParsingTest`

**File:** `src/test/java/org/api/automation/tests/json/JsonPathParsingTest.java`

**Extends:** `BaseTest`

**`@BeforeClass(alwaysRun = true) parsePayload()`:** Reads `testdata/course-price.json` once and parses it into a `JsonPath` instance shared by all test methods. `alwaysRun = true` is required: without it, TestNG silently skips this setup when the suite filters by group, and every test fails with `NullPointerException`.

All tests are offline (no network) and read from the `course-price.json` file which has 4 courses (Selenium Python, Cypress, RPA, Appium) and a `dashboard` with `purchaseAmount: 1162`.

#### `shouldReturnExpectedNumberOfCourses`

| Intent | `courses.size()` is a Groovy GPath call (not standard JSONPath). Asserts 4 courses. | Groups | `offline`, `smoke` |

#### `shouldReadNestedDashboardValue`

| Intent | Dot notation into a nested object: `dashboard.purchaseAmount` (int) and `dashboard.website` (String). | Groups | `offline`, `smoke` |

#### `shouldReadFirstCourseByIndex`

| Intent | Zero-based index access (`courses[0]`) and negative index (`courses[-1]` = last element, a Groovy convenience). | Groups | `offline` |

#### `shouldCollectAllCourseTitles`

| Intent | `courses.title` on an array returns all titles as a `List`. `contains()` matcher asserts exact contents and order. `hasItem()` asserts presence only. | Groups | `offline` |

#### `shouldFindCopiesSoldForSpecificCourse`

| Intent | `find { it.title == 'RPA' }.copies` — Groovy closure filter on the array. Asserts `copies == 10`. | Groups | `offline` |

#### `shouldFindAllCoursesAbovePriceThreshold`

| Intent | `findAll { it.price > 40 }.title` — returns titles of all courses with price > 40. Asserts exact list: `["Selenium Python", "RPA"]`. | Groups | `offline` |

#### `purchaseAmountShouldEqualSumOfCourseLineItems`

| Intent | The most important test in the class: computes `sum(price * copies)` over all courses and asserts it equals `dashboard.purchaseAmount`. A mismatch would indicate a real defect in how the API computes the total. The old code only printed whether they matched. | Groups | `offline`, `smoke` |

---

### 3.10 `SerializationTest`

**File:** `src/test/java/org/api/automation/tests/serialization/SerializationTest.java`

**Extends:** `BaseTest`

All tests are offline. They answer the question: "Is the POJO mapping correct?" independently of whether the API is up.

#### `shouldSerializePojoToApiFieldNames`

| Intent | Serializes an `AddPlaceRequest` to JSON and asserts that `@JsonProperty("phone_number")` works — the output contains `phone_number` (snake_case) and does NOT contain `phoneNumber` (camelCase). Also validates nested object structure, list content, and floating-point precision (delta 1e-4 to account for JSON round-trip through float). |
| Groups | `offline`, `smoke` |

#### `shouldDeserializeNestedResponseIntoPojoTree`

| Intent | Reads `oauth-course-details-sample.json`, deserializes it into `CourseDetailsResponse`, and asserts the three-level tree is correctly populated: instructor, expertise, 3 web courses, 2 API courses, 1 mobile course, 6 total. Asserts a specific course title and price using typed field access. |
| Groups | `offline`, `smoke` |

#### `shouldFindCourseByTitleInDeserializedTree`

| Intent | Calls `findCourseByTitle("SoapUI Webservices testing")` on the deserialized tree. Asserts the `Optional` is present and `priceAsInt() == 40`. Calls `findCourseByTitle("Course That Does Not Exist")` and asserts `isEmpty()`. |
| Groups | `offline` |

#### `webAutomationTitlesShouldMatchExpectedCatalogue`

| Intent | Calls `webAutomationTitles()` and asserts exact ordered list: `["Selenium Webdriver Java", "Cypress", "Protractor"]`. |
| Groups | `offline` |

#### `shouldToleratePreviouslyUnknownResponseFields`

| Intent | Proves `@JsonIgnoreProperties(ignoreUnknown = true)` works: deserializes a JSON string with an unknown field (`aFieldAddedByTheBackendTeamLastNight`) and asserts that known fields still bind correctly. Without the annotation, this would throw `UnrecognizedPropertyException`. |
| Groups | `offline` |

---

## 4. BDD (Cucumber) Flow

### How the Components Connect

```
CucumberTestRunner (TestNG @DataProvider)
    |
    +-- discovers: src/test/resources/features/placeValidations.feature
    |
    +-- glue package: org.api.automation.bdd.stepdefinitions
    |       |
    |       +-- Hooks (PicoContainer-injected ScenarioContext)
    |       +-- PlaceStepDefinitions (PicoContainer-injected ScenarioContext)
    |
    +-- PicoContainer creates ONE ScenarioContext per scenario
            |
            +-- injected into Hooks constructor
            +-- injected into PlaceStepDefinitions constructor
            (same instance, different classes, same scenario = shared state)
```

**PicoContainer dependency injection:** `cucumber-picocontainer` is on the classpath. When Cucumber creates a step-definition class, it inspects the constructor. If the constructor declares `ScenarioContext` as a parameter, PicoContainer provides an instance. All classes in the **same scenario** that declare `ScenarioContext` receive **the same instance**. A new scenario gets a **fresh instance**. This replaces static fields entirely.

### Feature File: `placeValidations.feature`

**Full path:** `src/test/resources/features/placeValidations.feature`

**Tags:** `@Places` (feature-level, applies to all scenarios). Additional per-scenario tags: `@AddPlace @Regression @Smoke`, `@DeletePlace @Regression`.

---

### Scenario 1: `A place can be created and retrieved with the values submitted`

This is a **Scenario Outline** with an `Examples` table. Cucumber expands it into three scenarios, one per row: Frontline/English/123 Main St, Backline/Spanish/456 Elm St, Sidewalk/French/789 Oak St.

**Step-by-step mapping:**

| Gherkin line | Step definition method | What it does |
|---|---|---|
| `Given Add Place Payload with "<name>" "<language>" "<address>"` | `addPlacePayloadWith(name, language, address)` | Calls `TestDataFactory.addPlace(name, language, address)`, attaches it as the body on a fresh Places spec, stores name and address in context. |
| `When User calls "ADD_PLACE" API with "POST" http request` | `userCallsApiWithHttpRequest("ADD_PLACE", "POST")` | Resolves `Paths.fromName("ADD_PLACE")` = `/maps/api/place/add/json`, retrieves the spec from context, calls `spec.post(path)`, stores response in context. |
| `Then API call is successful with status code 200` | `apiCallIsSuccessfulWithStatusCode(200)` | `assertEquals(response.getStatusCode(), 200)`. |
| `And "status" in response body is "OK"` | `keyInResponseBodyIs("status", "OK")` | `JsonUtils.getString(response, "status") == "OK"`. |
| `And "scope" in response body is "APP"` | `keyInResponseBodyIs("scope", "APP")` | `JsonUtils.getString(response, "scope") == "APP"`. |
| `Then place_Id is captured from the response` | `placeIdIsCapturedFromTheResponse()` | Extracts `place_id` from response, asserts not null, stores as `context.put("placeId", placeId)`. |
| `And the created place can be retrieved with name "<name>"` | `theCreatedPlaceCanBeRetrievedWithName(name)` | Reads `placeId` from context; builds a GET spec with `?place_id=<id>`; calls `userCallsApiWithHttpRequest("GET_PLACE", "GET")`; calls `apiCallIsSuccessfulWithStatusCode(200)`; asserts `name` field equals `<name>`. |
| `And the created place has address "<address>"` | `theCreatedPlaceHasAddress(address)` | Asserts `address` field in the response set by the previous compound step. |

---

### Scenario 2: `A place can be deleted`

**`@Before(value = "@DeletePlace", order = 10)` in `Hooks.java`** runs before this scenario:

1. Hooks creates a fresh place via a direct REST Assured call (not via a step).
2. Stores the `place_id` as `context.put("placeId", created.getPlaceId())`.
3. The scenario steps then operate on that id.

**Step-by-step mapping:**

| Gherkin line | Step definition method | What it does |
|---|---|---|
| `Given Delete Place Payload with the created place_Id` | `deletePlacePayloadWithCreatedPlaceId()` | Reads `placeId` from context (stored by the hook); builds a `DeletePlaceRequest`; attaches it as body on a fresh Places spec; stores spec in context. |
| `When User calls "DELETE_PLACE" API with "DELETE" http request` | `userCallsApiWithHttpRequest("DELETE_PLACE", "DELETE")` | Resolves `Paths.fromName("DELETE_PLACE")`, calls `spec.delete(path)`, stores response. |
| `Then API call is successful with status code 200` | `apiCallIsSuccessfulWithStatusCode(200)` | Asserts status. |
| `And "status" in response body is "OK"` | `keyInResponseBodyIs("status", "OK")` | Asserts `status` field. |

---

### Reports Generated

| Format | Location | Consumed by |
|---|---|---|
| JSON | `target/json-reports/cucumber-report.json` | `maven-cucumber-reporting` plugin (generates HTML) |
| HTML (Cucumber built-in) | `target/cucumber-reports/cucumber.html` | Browser, self-contained |
| HTML (maven-cucumber-reporting) | `target/cucumber-html-report/` | Jenkins "publish HTML" post-build step |
| Console pretty | Standard output | Developer / CI log |

---

## 5. Configuration and Secrets Resolution

### Three-Level Lookup (highest priority first)

```
1. JVM system property     -Dplaces.baseUrl=https://staging.example.com
2. OS environment variable  PLACES_BASEURL  (key upper-cased, dots and hyphens → underscores)
3. config/<env>.properties  places.baseUrl = https://rahulshettyacademy.com
```

For each key lookup, `ConfigManager.get(key)`:
1. Calls `System.getProperty(key)` — set by `-D` flags or `<systemPropertyVariables>` in Surefire.
2. Converts the key to an env-var name (`places.baseUrl` → `PLACES_BASEURL`) and calls `System.getenv(envVarName)`.
3. Falls back to the loaded `Properties` object from the classpath file.

Returns `null` (not exception) if the key is found nowhere. Use `getRequired()` for credentials.

### Key conversion rule

`oauth.client.secret` → `OAUTH_CLIENT_SECRET` (dots and hyphens become underscores, all uppercase).

### Environment selection

The `env` JVM property selects the properties file: `-Denv=staging` → `config/staging.properties`. Default is `qa`. The Surefire configuration passes the Maven `<env>` property as a system property so that `-Denv=staging` on the Maven command line reaches `ConfigManager`.

### `qa.properties` — What each key maps to

**File path:** `src/test/resources/config/qa.properties`

| Key | Value in QA | Notes |
|---|---|---|
| `places.baseUrl` | `https://rahulshettyacademy.com` | Root URI for all Places calls |
| `library.baseUrl` | `http://216.10.245.166` | Library API host |
| `ecom.baseUrl` | `https://rahulshettyacademy.com` | E-commerce API host |
| `oauth.baseUrl` | `https://rahulshettyacademy.com` | OAuth practice API host |
| `graphql.baseUrl` | `https://rahulshettyacademy.com` | GraphQL host |
| `google.token.url` | `https://www.googleapis.com/oauth2/v4/token` | Google token endpoint (authorization-code flow) |
| `places.apiKey` | `qaclick123` | Public practice key; not a real secret |
| `framework.logRequests` | `true` | Enables REST Assured traffic logging |
| `framework.relaxedHttps` | `true` | Accepts self-signed certificates |
| `framework.retryCount` | `1` | Times to retry a failed test |
| `oauth.scope` | `trust` | Scope for the OAuth practice server |
| `oauth.redirectUri` | `https://rahulshettyacademy.com/getCourse.php` | Redirect URI reference |

**Keys intentionally absent (secrets):** `ecom.userEmail`, `ecom.userPassword`, `oauth.clientId`, `oauth.clientSecret`.

### Running Against Different Environments

```bash
# QA (default)
mvn clean test

# Staging (requires staging.properties in src/test/resources/config/)
mvn clean test -Denv=staging

# Override a single URL at runtime without changing any file
mvn clean test -Dplaces.baseUrl=https://staging.rahulshettyacademy.com

# Supply secrets for the auth suite (PowerShell)
$env:ECOM_USEREMAIL     = "you@example.com"
$env:ECOM_USERPASSWORD  = "..."
$env:OAUTH_CLIENTID     = "...apps.googleusercontent.com"
$env:OAUTH_CLIENTSECRET = "..."
mvn clean test -DsuiteXmlFile=src/test/resources/suites/auth.xml

# Supply secrets for the auth suite (Jenkins)
# Use the Credentials Binding plugin to set ECOM_USEREMAIL, etc.
# Then in the Maven step: mvn clean test -DsuiteXmlFile=src/test/resources/suites/auth.xml
```

---

## 6. Data Sources

### 6.1 TestDataFactory

**File:** `src/main/java/org/api/automation/data/TestDataFactory.java`

| Method | What it generates | Why |
|---|---|---|
| `addPlace(name, language, address)` | `AddPlaceRequest` with the three supplied fields and defaults for accuracy (50), phone number, website, types, and location | Tests only need to supply the fields they assert on; defaults prevent tests from breaking when a new required field is added |
| `defaultPlace()` | `AddPlaceRequest` with all fields defaulted to "Frontline House" / "English" / "29, side layout, cohen 09" | For tests that do not care about content at all |
| `uniqueBook(aisle)` | `AddBookRequest` with `isbn = "isbn" + uniqueSuffix()` and supplied aisle | The Library API derives the book id from `isbn + aisle` and rejects duplicates; a fixed isbn fails on the second run |
| `book(isbn, aisle)` | `AddBookRequest` with caller-supplied isbn | When the test asserts on the exact id rather than just its presence |
| `uniqueSuffix()` | `String`: `System.currentTimeMillis() + random(1000–9999)` | Millisecond timestamp alone is not safe for parallel threads; the random component prevents collision |

---

### 6.2 ExcelDataReader

**File:** `src/main/java/org/api/automation/data/ExcelDataReader.java`

**Spreadsheet used:** `src/test/resources/testdata/TestData.xlsx`, sheet `TestData`.

**Expected sheet format:**

| TestCases | Data1 | Data2 | Data3 | Data4 |
|---|---|---|---|---|
| AddBook | Appium | John Doe | isbn-prefix | 227 |
| Login | abc | password | ... | ... |

Row 0 is the header. The `TestCases` column is the key column. `ExcelDataReader.getRow(workbook, sheet, "TestCases", "AddBook")` returns `{"TestCases": "AddBook", "Data1": "Appium", "Data2": "John Doe", "Data3": "isbn-prefix", "Data4": "227"}`.

**Numeric cell handling:** Apache POI stores numeric cells (including what looks like integers) as doubles. `NumberToTextConverter.toText()` renders them the way Excel displays them: `227.0` (the double) becomes `"227"` (the string), preventing API rejections of numeric aisles.

---

### 6.3 GraphQLQueries

**File:** `src/main/java/org/api/automation/data/GraphQLQueries.java`

All queries are Java text blocks (multiline string literals, no escaping). They all use GraphQL introspection so they pass against any spec-compliant server.

| Constant | Document | Used in |
|---|---|---|
| `SCHEMA_QUERY_TYPE` | `{ __schema { queryType { name } } }` | `shouldExecuteMinimalQuery`, `validQueryShouldReturnDataAndNoErrors` |
| `DISCOVER_QUERY_FIELDS` | Full `__schema.queryType.fields` tree | `shouldDiscoverAvailableQueryFields` |
| `DISCOVER_MUTATION_FIELDS` | `__schema.mutationType.fields` | `shouldReportWhetherEndpointSupportsMutations` |
| `TYPE_INFO_BY_NAME` | Parameterised: `query TypeInfo($typeName: String!)` | `shouldExecuteQueryWithVariables`, `variableValueShouldDetermineTheResult`, `shouldReportErrorWhenMandatoryVariableIsMissing` |
| `INVALID_FIELD` | Asks for `thisFieldDoesNotExistAnywhere` | `shouldReportErrorsInEnvelopeForInvalidField` |
| `MALFORMED_DOCUMENT` | Unclosed brace | `shouldReportErrorForMalformedDocument` |

---

### 6.4 JSON Payload Templates

**Directory:** `src/main/resources/payloads/`

#### `add-book.json`

```json
{
  "name": "Learn API Automation with Java",
  "isbn": "{{isbn}}",
  "aisle": "{{aisle}}",
  "author": "John Cena"
}
```

Placeholders: `{{isbn}}`, `{{aisle}}`. Used in `BookPayloadTest.shouldAddBookFromJsonFileTemplate()` via `ResourceReader.readJsonTemplate("payloads/add-book.json", "isbn", generatedIsbn, "aisle", "2645")`.

#### `add-place.json`

A fully filled-in static payload (no placeholders). Used as a reference or for cases where the exact values do not need to vary. It is not substituted at runtime in the current test suite but is available for tests that need a known-good body.

```json
{
  "location": { "lat": -38.383494, "lng": 33.427362 },
  "accuracy": 50,
  "name": "Frontline house",
  "phone_number": "(+91) 983 893 3937",
  "address": "29, side layout, cohen 09",
  "types": ["shoe park", "shop"],
  "website": "https://google.com",
  "language": "French-IN"
}
```

#### `delete-book.json`

```json
{
  "ID": "{{bookId}}"
}
```

Placeholder: `{{bookId}}`. Not used directly in the current tests (the tests construct `DeleteBookRequest` POJOs instead), but available for template-based deletion.

---

### 6.5 Test Data Files

**Directory:** `src/test/resources/testdata/`

#### `course-price.json`

Used by `JsonPathParsingTest`. Contains a `dashboard` object (purchaseAmount: 1162, website: "rahulshettyacademy.com") and a `courses` array of 4 entries:

| title | price | copies | price × copies |
|---|---|---|---|
| Selenium Python | 50 | 6 | 300 |
| Cypress | 40 | 4 | 160 |
| RPA | 45 | 10 | 450 |
| Appium | 36 | 7 | 252 |
| **Total** | | | **1162** |

The total (1162) matches `dashboard.purchaseAmount`, which is what `purchaseAmountShouldEqualSumOfCourseLineItems` verifies.

#### `oauth-course-details-sample.json`

Used by `SerializationTest` and `OAuthTest` (for offline variants). Contains the structure returned by the OAuth `/getCourseDetails` endpoint: instructor (RahulShetty), expertise, url, linkedIn, and a `courses` object with:
- `webAutomation`: 3 entries (Selenium Webdriver Java/50, Cypress/40, Protractor/40)
- `api`: 2 entries (Rest Assured Automation using Java/50, SoapUI Webservices testing/40)
- `mobile`: 1 entry (Appium-Mobile Automation using Java/50)

#### `TestData.xlsx`

Sheet `TestData`. At minimum two rows: one keyed `AddBook` and one keyed `Login`. See the `ExcelDataReader` section for column layout.

#### `laptop.jpg`

Binary image file used as the product image in `EcomOrderE2ETest.shouldAddProductWithImageUpload()`. Loaded via `ResourceReader.asFile("testdata/laptop.jpg")` which resolves it through the classloader.

---

## 7. Request/Response POJO Catalogue

### Convention Summary

| Convention | Applied as | Why |
|---|---|---|
| `@JsonIgnoreProperties(ignoreUnknown = true)` | On every response POJO | Prevents `UnrecognizedPropertyException` when the backend adds a new field |
| `@JsonProperty("snake_case_name")` | On fields where the Java name and API name differ | Maps Java camelCase to the API's snake_case without forcing ugly Java field names |
| `@JsonAlias({...})` | On response fields with multiple known API names | Accepts multiple casings (e.g. `msg`, `Msg`, `MSG`) without multiple POJOs |
| No-arg constructor | All POJOs | Required by Jackson for deserialization |
| `toString()` with masking | `LoginRequest`, `LoginResponse`, `OAuthTokenResponse` | Prevents credentials appearing in logs |

### Request POJOs

| Class | Fields | Jackson annotations |
|---|---|---|
| `AddBookRequest` | `name`, `isbn`, `aisle`, `author` (all String) | None (API names match) |
| `AddPlaceRequest` | `location` (Location), `accuracy` (int), `name`, `phoneNumber`, `address` (String), `types` (List\<String\>), `website`, `language` (String) | `@JsonProperty("phone_number")` on `phoneNumber` |
| `CreateOrderRequest` | `orders` (List\<OrderDetail\>) | None |
| `DeleteBookRequest` | `id` (String) | `@JsonProperty("ID")` — API expects uppercase key |
| `DeletePlaceRequest` | `placeId` (String) | `@JsonProperty("place_id")` |
| `GraphQLRequest` | `query` (String), `variables` (Map\<String, Object\>), `operationName` (String) | None (names are per the GraphQL-over-HTTP spec) |
| `Location` | `lat`, `lng` (both double) | None |
| `LoginRequest` | `userEmail`, `userPassword` (both String) | None; `toString()` masks password |
| `OrderDetail` | `country`, `productOrderedId` (both String) | None |
| `UpdatePlaceRequest` | `placeId` (String), `address` (String), `key` (String) | `@JsonProperty("place_id")` on `placeId` |

### Response POJOs

| Class | Fields | Jackson annotations | Convenience methods |
|---|---|---|---|
| `AddBookResponse` | `message` (String), `id` (String) | `@JsonIgnoreProperties`; `@JsonProperty("Msg")` + `@JsonAlias({"msg","message"})` on message; `@JsonProperty("ID")` + `@JsonAlias({"id"})` on id | — |
| `AddPlaceResponse` | `status`, `placeId`, `scope`, `reference`, `id` (all String) | `@JsonIgnoreProperties`; `@JsonProperty("place_id")` on `placeId` | — |
| `AddProductResponse` | `message`, `productId` (both String) | `@JsonIgnoreProperties` | — |
| `Course` | `courseTitle` (String), `price` (String) | `@JsonIgnoreProperties` | `priceAsInt()` — parses price as int |
| `CourseCatalog` | `webAutomation`, `api`, `mobile` (all List\<Course\>) | `@JsonIgnoreProperties` | `all()` — combines all three lists |
| `CourseDetailsResponse` | `instructor`, `url`, `services`, `expertise`, `linkedIn` (all String), `courses` (CourseCatalog) | `@JsonIgnoreProperties` | `findCourseByTitle(String)` — Optional\<Course\>; `webAutomationTitles()` — List\<String\> |
| `CreateOrderResponse` | `message` (String), `orders` (List\<String\>), `productOrderId` (List\<String\>) | `@JsonIgnoreProperties` | `firstOrderId()` — first element or null |
| `GeoLocation` | `latitude`, `longitude` (both double) | `@JsonIgnoreProperties` | `toString()` — `"(lat, lng)"` |
| `GetPlaceResponse` | `location` (GeoLocation), `accuracy` (int), `name`, `phoneNumber`, `address`, `types` (all String), `website`, `language` (String) | `@JsonIgnoreProperties`; `@JsonProperty("phone_number")` on `phoneNumber` | `typesList()` — splits comma-delimited `types` into List\<String\> |
| `GraphQLResponse` | `data` (Map\<String, Object\>), `errors` (List\<GraphQLError\>) | `@JsonIgnoreProperties` | `hasErrors()`, `errorMessages()` |
| `GraphQLResponse.GraphQLError` (inner) | `message` (String), `locations` (List\<Map\>), `path` (List\<Object\>) | `@JsonIgnoreProperties` | `toString()` — the message |
| `LoginResponse` | `token`, `userId`, `message` (all String) | `@JsonIgnoreProperties` | `toString()` — masks token |
| `MessageResponse` | `message`, `status` (both String) | `@JsonIgnoreProperties`; `@JsonProperty("message")` + `@JsonAlias({"msg","Msg","MSG"})` on `message` | `toString()` |
| `OAuthTokenResponse` | `accessToken`, `tokenType` (String), `expiresIn` (Long), `refreshToken`, `scope` (String) | `@JsonIgnoreProperties`; `@JsonProperty` on all four snake_case fields | `toString()` — masks access token |

### Asymmetry: `Location` vs `GeoLocation`

The Places API demonstrates a common real-world inconsistency:

| Direction | Object | Fields |
|---|---|---|
| Request (Add Place) | `Location` | `lat`, `lng` |
| Response (Get Place) | `GeoLocation` | `latitude`, `longitude` |

Using a single POJO for both directions fails at deserialization. Separate models let each match the wire format honestly.

### Why `types` is a String in `GetPlaceResponse`

Add Place accepts `"types": ["shoe park", "shop"]` (JSON array). Get Place returns `"types": "shoe park,shop"` (a comma-separated String). Declaring it as `List<String>` produces `MismatchedInputException`. The model matches the wire format; `typesList()` handles splitting for callers.

---

## 8. Test Suites

Suites live in `src/test/resources/suites/`. The active suite is selected with `-DsuiteXmlFile=<path>`. The default is `src/test/resources/suites/testng.xml`.

---

### `testng.xml` — Default Suite

```
mvn clean test
```

**Suite attributes:** `parallel="classes"`, `thread-count="4"`, `data-provider-thread-count="3"`.

`parallel="classes"` runs each test class on its own thread — safe because there is no shared mutable state between classes (specs are fresh per call, chained state is in instance fields). `parallel="methods"` would break `PlaceCrudE2ETest` because its five methods must run sequentially.

**Two `<test>` blocks:**

1. `API Tests` — runs the entire `org.api.automation.tests.*` package tree, excluding groups `requires-credentials` (needs secrets not in the repo) and `needs-verification` (behaviour not yet confirmed against the live API).
2. `BDD Scenarios` — runs `CucumberTestRunner`, which picks up all feature files.

**Groups excluded:**

| Group | Reason for exclusion |
|---|---|
| `requires-credentials` | Needs live OAuth client secret or e-commerce login; not in the repository |
| `needs-verification` | Asserts behaviour nobody has confirmed against the live API; would produce meaningless failures |

---

### `smoke.xml` — Smoke Suite

```
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
```

**Purpose:** Fast confidence check — is each API reachable and responding on its main path? Run on every commit.

**Groups included:** `smoke`

**Groups excluded:** `requires-credentials`, `needs-verification`

**Tests in the `smoke` group (from the source annotations):**

| Test class | Method |
|---|---|
| `AddPlaceTest` | `shouldAddPlaceAndReturnExpectedFields`, `shouldReturnUsablePlaceIdInTypedResponse` |
| `GraphQLTest` | `shouldExecuteMinimalQuery`, `shouldReportErrorsInEnvelopeForInvalidField`, `validQueryShouldReturnDataAndNoErrors` |
| `JsonPathParsingTest` | `shouldReturnExpectedNumberOfCourses`, `shouldReadNestedDashboardValue`, `purchaseAmountShouldEqualSumOfCourseLineItems` |
| `SerializationTest` | `shouldSerializePojoToApiFieldNames`, `shouldDeserializeNestedResponseIntoPojoTree` |

---

### `offline.xml` — Offline Suite

```
mvn clean test -DsuiteXmlFile=src/test/resources/suites/offline.xml
```

**Purpose:** No network required. Covers JsonPath parsing, serialization mapping, spreadsheet reading, and URL parsing. Runs in a couple of seconds and makes an ideal pre-commit hook. If these fail, the framework is broken; if they pass, the problem is the environment or the API.

**Groups included:** `offline` only.

**Tests in the `offline` group:**

| Test class | Methods |
|---|---|
| `JsonPathParsingTest` | All (parsePayload `@BeforeClass alwaysRun=true` runs too) |
| `SerializationTest` | `shouldFindCourseByTitleInDeserializedTree`, `webAutomationTitlesShouldMatchExpectedCatalogue`, `shouldToleratePreviouslyUnknownResponseFields`, plus the two smoke-tagged ones |
| `ExcelDataDrivenTest` | `shouldReadRowsByKeyNotByPosition`, `shouldFailClearlyWhenTestCaseRowIsMissing` |
| `OAuthTest` | `shouldExtractAuthorizationCodeFromRedirectUrl`, `shouldExtractCodeRegardlessOfParameterOrder` |

---

### `auth.xml` — Auth Suite

```
$env:OAUTH_CLIENTID = "..."
$env:OAUTH_CLIENTSECRET = "..."
$env:ECOM_USEREMAIL = "..."
$env:ECOM_USERPASSWORD = "..."
mvn clean test -DsuiteXmlFile=src/test/resources/suites/auth.xml
```

**Purpose:** Credential-dependent tests: OAuth 2.0 token flow and e-commerce end-to-end.

**Groups included:** `auth`, `e2e`.

**Explicit classes:** `OAuthTest`, `EcomOrderE2ETest` (not the whole package, to avoid accidentally pulling in other tests).

**Not parallel:** OAuth providers rate-limit token endpoints. `OAuthTest` caches the token in an instance field and reuses it within the class, reducing token calls.

**What happens if a secret is missing:** `ConfigManager.getRequired()` throws `IllegalStateException` immediately with a message naming the config key, the `-D` flag form, and the environment variable form. The test fails before making any API call, rather than sending an empty credential and reporting a confusing 401.

---

### Suite Comparison Matrix

| Suite | Network | Credentials | Speed | When to run |
|---|---|---|---|---|
| `offline.xml` | No | No | Seconds | Pre-commit hook; debugging framework |
| `smoke.xml` | Yes | No | Fast | Every commit; post-deployment |
| `testng.xml` (default) | Yes | No | Medium | Full regression without secrets |
| `auth.xml` | Yes | Yes | Slow | When credentials are available; scheduled job |

---

### Log Files

| File | Content | Written by |
|---|---|---|
| `target/logs/api-traffic.log` | Full HTTP request and response bodies for every call | REST Assured `RequestLoggingFilter` / `ResponseLoggingFilter` in `BaseTest` and BDD specs |
| `target/logs/framework.log` | Test lifecycle events, errors, and info messages | Log4j2 `File` appender |
| Console | Same as `framework.log` | Log4j2 `Console` appender |
| `target/cucumber-html-report/` | Rich HTML Cucumber report | `maven-cucumber-reporting` plugin (runs in `verify` phase) |

On test failure, `TestListener.onTestFailure()` logs: `Full request/response traffic: target/logs/api-traffic.log` — so the first place to look for what the failing test actually sent and received is that file.
