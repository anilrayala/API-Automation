# API Automation Framework

REST Assured + TestNG + Cucumber. Covers REST and GraphQL, token and OAuth 2.0 auth,
POJO serialization, data-driven testing from code and spreadsheets, BDD, and Jenkins
reporting.

---

## Quick start

```bash
mvn clean test                                                    # default suite
mvn clean test -DsuiteXmlFile=src/test/resources/suites/offline.xml  # no network needed
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
mvn clean verify                                                  # + Cucumber HTML report
```

**Requires JDK 21 or newer.** `JAVA_HOME` currently points at `C:\Program Files\Java\jdk-23`,
which is no longer installed — set it to your JDK 25 install or Maven will not start:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
```

---

## Layout

```
src/main/java/org/api/automation/     ← the framework (reusable, no test logic)
  config/       ConfigManager, ConfigKeys      environment + secrets resolution
  constants/    ApiEndpoint, HttpMethod        every URL and verb, in one place
  core/         SpecFactory, RestClient        how a request is built and fired
  models/       request/ + response/           POJOs for serialization
  services/     PlaceService, EcomService…     one class per API, one method per operation
  data/         TestDataFactory, ExcelDataReader, GraphQLQueries
  utils/        JsonUtils, ResourceReader
  exceptions/   FrameworkException

src/test/java/org/api/automation/      ← the tests
  base/         BaseTest
  listeners/    TestListener, RetryAnalyzer, RetryListener
  tests/        json/ serialization/ place/ library/ auth/ ecom/ graphql/
  bdd/          context/ stepdefinitions/ runner/

src/test/resources/
  config/qa.properties      per-environment settings (NO secrets)
  suites/*.xml              testng.xml, offline.xml, smoke.xml, auth.xml
  features/                 Cucumber feature files
  testdata/                 TestData.xlsx, laptop.jpg, sample JSON
  log4j2.xml
```

The flow is always the same:

```
Test  →  Service  →  RestClient  →  SpecFactory  →  HTTP
 ↑ asserts    ↑ one method     ↑ logs +      ↑ base URI, auth,
              per operation      dispatches    content type, filters
```

A test never builds a URL, sets a header, or knows an endpoint path.

---

## Configuration and secrets

Everything configurable lives in `src/test/resources/config/qa.properties`. Values resolve
in this order, first match wins:

1. `-Dplaces.baseUrl=...` on the Maven command line
2. environment variable `PLACES_BASEURL` (key upper-cased, `.` → `_`)
3. the properties file

**No secret is in this repository.** The auth tests need these set as environment variables:

```powershell
$env:OAUTH_CLIENTID     = "...apps.googleusercontent.com"
$env:OAUTH_CLIENTSECRET = "..."
$env:ECOM_USEREMAIL     = "you@example.com"
$env:ECOM_USERPASSWORD  = "..."
```

Then `mvn test -DsuiteXmlFile=src/test/resources/suites/auth.xml`.

A missing value fails immediately naming the key, rather than sending an empty credential
and reporting a confusing 401.

> ⚠️ **Rotate the old Google client secret.** It was hardcoded in
> `src/main/java/files/OAuthTest.java` and `DeserializedOAuthTest.java`. Removing it from
> the working tree does not remove it from git history — anyone with the repo can still
> read it. Revoke it in the Google Cloud console and issue a new one.

Add a new environment by dropping `staging.properties` next to `qa.properties` and running
`-Denv=staging`. No Java changes.

---

## Test groups

| Group | Meaning |
|---|---|
| `offline` | No network. JsonPath, serialization, Excel, URL parsing. |
| `smoke` | Fast main-path check per API. |
| `regression` | Full functional coverage. |
| `e2e` | Multi-call flows with chained state. |
| `datadriven` | DataProvider- or spreadsheet-driven. |
| `auth`, `graphql`, `negative` | By subject. |
| `external-api` | Depends on the Library practice host, which is often down. |
| `requires-credentials` | Needs a real secret. Excluded from the default suite. |
| `needs-verification` | Asserts behaviour not yet confirmed against the live API. Excluded. |

```bash
mvn test -Dgroups=smoke
mvn test -DexcludedGroups=external-api
```

---

## Cucumber

```bash
mvn test -Dcucumber.filter.tags="@AddPlace"
mvn test -Dcucumber.filter.tags="@Regression and not @DeletePlace"
mvn verify   # generates target/cucumber-html-report/
```

The runner extends `AbstractTestNGCucumberTests`, so scenarios are TestNG tests. This is
deliberate: Maven Surefire uses **one** test provider per module, so a JUnit 4 Cucumber
runner alongside TestNG tests means one of the two silently never runs while the build
still reports success.

Scenarios run in parallel. That is only safe because there is no static state — each
scenario gets its own `ScenarioContext` from PicoContainer.

---

## Reports and logs

| Path | Contents |
|---|---|
| `target/surefire-reports/index.html` | TestNG results |
| `target/cucumber-html-report/` | Cucumber HTML (after `mvn verify`) |
| `target/json-reports/cucumber-report.json` | JSON for Jenkins |
| `target/logs/framework.log` | One line per call: verb, path, status, duration |
| `target/logs/api-traffic.log` | Full request and response bodies |

Two log files on purpose — the readable trace stays readable, and the verbose payload dumps
are there when a failure needs them.

---

## What changed from the previous version

| Before | After | Why |
|---|---|---|
| `public static void main` in `src/main/java` | TestNG tests in `src/test/java` | Nothing ran the `main` methods. |
| `//@Test`, `//Assert.assertEquals` everywhere | Real assertions | TestNG wasn't a dependency, so every assertion was dead code and nothing was validated. |
| `Utils` cached one shared `RequestSpecification` | `SpecFactory` builds a fresh one per call | Specs are **mutable**. Callers did `.body()`/`.queryParam()` on the shared instance, so params accumulated across scenarios and bodies leaked between calls. |
| `static String placeId` shared with `Hooks` | `ScenarioContext` via PicoContainer | Static state leaked between scenarios and blocked parallel runs. `Hooks` also did `new StepDefinition()`, an object Cucumber never used. |
| Secrets in source | `ConfigManager` + env vars | A live Google secret and an ecom password were compiled into class files. |
| `RestAssured.baseURI = "..."` | Per-service base URI from config | A global breaks the moment two APIs are on different hosts — which they are here. |
| Selenium + WebDriverManager | Removed | Driving Google's consent screen is slow, blocked, and not your code. `OAuthService.exchangeAuthorizationCode` tests the part that is yours. |
| `if/else if` over method name | `HttpMethod` enum | Four copies of the same `given().spec().when()` chain. |
| Three identical `Course` POJOs | One `Course` | Jackson binds by shape, not by the key holding the array. |
| `data.get(1)`, `data.get(2)` from Excel | Header-keyed `Map` | Inserting a spreadsheet column silently shifted every index. |
| JUnit 4 + TestNG | TestNG only | Surefire runs one provider; the other would be skipped silently. |
| `logging.txt` in repo root, committed | `target/logs/` | Build output does not belong in version control. |
| Fixed isbns in data providers | Generated per run | The API rejects duplicates, so those tests passed once, ever. |
| GraphQL documented, never coded | 9 working tests | It was the one topic in the README with no implementation. |

---

## Three API quirks the models encode

Found by running the tests, not by reading docs:

1. **Places is asymmetric.** Add Place accepts `{"lat", "lng"}`; Get Place returns
   `{"latitude", "longitude"}`. Hence separate `Location` (request) and `GeoLocation`
   (response) — the reason request and response models are not shared.
2. **`types` changes shape.** Sent as an array `["shoe park","shop"]`, returned as a string
   `"shoe park,shop"`. `GetPlaceResponse.types` is a `String` with a `typesList()` helper —
   model what the API sends, not what you wish it sent.
3. **GraphQL errors are not always HTTP 200.** The common claim that "GraphQL always
   returns 200" is only true for *field* errors. This endpoint returns **400** for a
   validation error. The reliable invariant is the `errors` array in the body, never the
   status code — which is why `GraphQLService.execute()` asserts no status at all.

---

## Extending it

**A new endpoint** — add to `ApiEndpoint`, add a method to the relevant service, write the
test. Nothing else changes.

**A new API** — add a base URL to `ConfigKeys` and `qa.properties`, add its endpoints, add a
service class.

**Real GraphQL queries** — run `GraphQLTest.shouldDiscoverAvailableQueryFields`; it logs
every root field the server exposes. Add documents to `GraphQLQueries` using those names.
The current queries use introspection so they pass against any GraphQL server without
depending on a schema staying live.

---

## Jenkins

Maven project, goals `clean verify`. Post-build → Publish Cucumber reports, JSON path
`target/json-reports/cucumber-report.json`.

For tag filtering, tick *This project is parameterized*, add a Choice parameter named `tag`
with values `@AddPlace`, `@DeletePlace`, `@Regression`, and append to the goals:

```
-Dcucumber.filter.tags="$tag"
```

Inject secrets with the Credentials Binding plugin as `OAUTH_CLIENTID`,
`OAUTH_CLIENTSECRET`, `ECOM_USEREMAIL`, `ECOM_USERPASSWORD` — never as build parameters,
which are visible in the build log.

Jenkins supports Java 17, 21 and 25. Start it with
`java -jar jenkins.war --enable-future-java` if it warns about the JDK.

---

## Gherkin reference

`Feature` list of scenarios · `Scenario` one business rule · `Given` precondition ·
`When` action · `Then` expected outcome · `And`/`But` continuation ·
`Scenario Outline` + `Examples` one run per data row · `Background` steps before every
scenario · `"""` doc string · `|` data table · `@tag` filtering · `<placeholder>` outline
substitution · `#` comment.
