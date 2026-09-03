package org.api.automation.tests.graphql;

import io.restassured.response.Response;
import org.api.automation.base.BaseTest;
import org.api.automation.constants.Paths;
import org.api.automation.data.GraphQLQueries;
import org.api.automation.models.request.GraphQLRequest;
import org.api.automation.models.response.GraphQLResponse;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * GraphQL testing — what differs from REST:
 *
 * <ul>
 *   <li>One endpoint, always POST, always the same URL.</li>
 *   <li>Failures live in the response <b>body</b> ({@code errors} array), not the status code.
 *       A server can return 200 for an operation that failed, and 400 for a validation error
 *       — this endpoint does both. Always check the errors array.</li>
 *   <li>Variables go in the JSON body, not string-concatenated into the document.</li>
 * </ul>
 *
 * <p>The queries here use introspection ({@code __schema}, {@code __type}), so they pass
 * against any GraphQL server without depending on a specific schema being live.
 */
public class GraphQLTest extends BaseTest {

    /** Connectivity plus the smallest valid document. */
    @Test(groups = {"graphql", "smoke"})
    public void shouldExecuteMinimalQuery() {
        GraphQLResponse response = query(GraphQLQueries.SCHEMA_QUERY_TYPE);

        assertFalse(response.hasErrors(), "Unexpected errors: " + response.errorMessages());
        assertNotNull(response.getData(), "data");

        String queryTypeName = nestedString(response, "__schema", "queryType", "name");
        assertThat("Root query type name", queryTypeName, not(emptyOrNullString()));
        log.info("Root query type is '{}'", queryTypeName);
    }

    /**
     * Prints the query fields the server exposes.
     * Run this first to find real field names, then write domain queries against them.
     */
    @Test(groups = {"graphql"})
    public void shouldDiscoverAvailableQueryFields() {
        GraphQLResponse response = query(GraphQLQueries.DISCOVER_QUERY_FIELDS);

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.getData().get("__schema");
        @SuppressWarnings("unchecked")
        Map<String, Object> queryType = (Map<String, Object>) schema.get("queryType");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) queryType.get("fields");

        assertNotNull(fields, "A schema must expose at least one root query field");
        assertFalse(fields.isEmpty(), "A schema must expose at least one root query field");

        log.info("This endpoint exposes {} root query field(s):", fields.size());
        fields.forEach(field -> log.info("  - {}", field.get("name")));
    }

    /** A query with variables — the parameterisation mechanism. */
    @Test(groups = {"graphql", "regression"})
    public void shouldExecuteQueryWithVariables() {
        GraphQLResponse response = query(GraphQLQueries.TYPE_INFO_BY_NAME, Map.of("typeName", "String"));

        assertFalse(response.hasErrors(), "Unexpected errors: " + response.errorMessages());
        assertEquals(nestedString(response, "__type", "name"), "String");
        assertEquals(nestedString(response, "__type", "kind"), "SCALAR", "String is a scalar");
    }

    /** Same query, different variable value — proves the variable is what varies. */
    @Test(groups = {"graphql", "regression"})
    public void variableValueShouldDetermineTheResult() {
        GraphQLResponse asString  = query(GraphQLQueries.TYPE_INFO_BY_NAME, Map.of("typeName", "String"));
        GraphQLResponse asBoolean = query(GraphQLQueries.TYPE_INFO_BY_NAME, Map.of("typeName", "Boolean"));

        assertEquals(nestedString(asString, "__type", "name"), "String");
        assertEquals(nestedString(asBoolean, "__type", "name"), "Boolean");
    }

    /** A mandatory variable that is not supplied must be rejected. */
    @Test(groups = {"graphql", "regression", "negative"})
    public void shouldReportErrorWhenMandatoryVariableIsMissing() {
        GraphQLResponse response = rawQuery(new GraphQLRequest(GraphQLQueries.TYPE_INFO_BY_NAME, Map.of()))
                .as(GraphQLResponse.class);

        assertTrue(response.hasErrors(),
                "A missing non-null variable must produce an error, but none was reported");
        log.info("Server reported: {}", response.errorMessages());
    }

    /**
     * The most important test: a GraphQL failure is in the errors array, not the status code.
     *
     * <p>I originally wrote this asserting HTTP 200. It failed: this endpoint returns 400.
     * The GraphQL-over-HTTP spec permits either, so status assertions are fragile.
     * The errors array is the invariant that holds on every server.
     */
    @Test(groups = {"graphql", "smoke", "negative"})
    public void shouldReportErrorsInEnvelopeForInvalidField() {
        Response raw = rawQuery(new GraphQLRequest(GraphQLQueries.INVALID_FIELD));

        log.info("Invalid field returned HTTP {}", raw.getStatusCode());

        GraphQLResponse parsed = raw.as(GraphQLResponse.class);
        assertTrue(parsed.hasErrors(),
                "An unknown field must be reported in the errors array, whatever the status code");
        assertNull(parsed.getData(), "A validation failure means no data was produced");

        log.info("Server reported: {}", parsed.errorMessages());
    }

    /** The other half: a successful operation must have no errors. */
    @Test(groups = {"graphql", "smoke"})
    public void validQueryShouldReturnDataAndNoErrors() {
        Response raw = rawQuery(new GraphQLRequest(GraphQLQueries.SCHEMA_QUERY_TYPE));

        assertEquals(raw.getStatusCode(), 200, "A valid operation should return 200");

        GraphQLResponse parsed = raw.as(GraphQLResponse.class);
        assertFalse(parsed.hasErrors(), "Unexpected errors: " + parsed.errorMessages());
        assertNotNull(parsed.getData(), "A successful operation must return data");
    }

    /** A syntactically broken document — rejected at parse time. */
    @Test(groups = {"graphql", "negative"})
    public void shouldReportErrorForMalformedDocument() {
        Response raw = rawQuery(new GraphQLRequest(GraphQLQueries.MALFORMED_DOCUMENT));
        log.info("Malformed document returned status {}", raw.getStatusCode());

        GraphQLResponse parsed = raw.as(GraphQLResponse.class);
        assertTrue(parsed.hasErrors(), "A syntax error must be reported in the errors array");
    }

    /** Reports whether this endpoint supports mutations. */
    @Test(groups = {"graphql"})
    public void shouldReportWhetherEndpointSupportsMutations() {
        GraphQLResponse response = query(GraphQLQueries.DISCOVER_MUTATION_FIELDS);

        Object mutationType = response.getData().get("__schema") == null
                ? null
                : ((Map<?, ?>) response.getData().get("__schema")).get("mutationType");

        if (mutationType == null) {
            log.info("This endpoint is read-only — it declares no mutation type");
        } else {
            log.info("Mutations available: {}", mutationType);
        }
    }

    // ------------------------------------------------------------------
    // Helpers — kept in this class because they are GraphQL-specific
    // ------------------------------------------------------------------

    /** POST a document and return the parsed envelope. Asserts no errors. */
    private GraphQLResponse query(String document) {
        GraphQLResponse response = rawQuery(new GraphQLRequest(document)).as(GraphQLResponse.class);
        assertFalse(response.hasErrors(), "Unexpected GraphQL errors: " + response.errorMessages());
        return response;
    }

    /** POST a document with variables. Asserts no errors. */
    private GraphQLResponse query(String document, Map<String, Object> variables) {
        GraphQLResponse response = rawQuery(new GraphQLRequest(document, variables)).as(GraphQLResponse.class);
        assertFalse(response.hasErrors(), "Unexpected GraphQL errors: " + response.errorMessages());
        return response;
    }

    /** POST a request and return the raw response — use this when errors are what you're testing. */
    private Response rawQuery(GraphQLRequest request) {
        return given(graphqlSpec())
                .body(request)
                .post(Paths.GRAPHQL);
    }

    /** Walks a nested path through the data map. */
    private String nestedString(GraphQLResponse response, String... path) {
        Object current = response.getData();
        for (String key : path) {
            assertNotNull(current, "Path " + String.join(".", path) + " hit null at '" + key + "'");
            current = ((Map<?, ?>) current).get(key);
        }
        return current == null ? null : current.toString();
    }
}
