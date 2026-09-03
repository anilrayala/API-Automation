package org.api.automation.models.request;

import java.util.Map;

/**
 * The body of a GraphQL request.
 *
 * <p>This is the structural difference from REST that trips people up: GraphQL has one
 * endpoint and one HTTP verb (POST). What varies is not the URL but this body, which always
 * has the same three fields:
 * <ul>
 *   <li>{@code query} — the query or mutation document</li>
 *   <li>{@code variables} — values referenced as {@code $name} inside the document, so the
 *       document itself stays a constant instead of being string-concatenated per test</li>
 *   <li>{@code operationName} — which operation to run when the document defines several</li>
 * </ul>
 *
 * <p>Because the URL never changes, you cannot assert on status code alone: GraphQL
 * returns HTTP 200 even for a failed operation and reports the failure in an
 * {@code errors} array. See {@code GraphQLResponse}.
 */
public class GraphQLRequest {

    private String query;
    private Map<String, Object> variables;
    private String operationName;

    public GraphQLRequest() {
        // required by Jackson
    }

    /** A query or mutation with no variables. */
    public GraphQLRequest(String query) {
        this.query = query;
    }

    /** A parameterised query or mutation — the preferred form. */
    public GraphQLRequest(String query, Map<String, Object> variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
