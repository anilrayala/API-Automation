package org.api.automation.data;

/**
 * GraphQL documents, kept as constants rather than inlined in tests.
 *
 * <p>Same reasoning as the payload files: a query is test data, and holding it in one place
 * means a schema change is one edit. Java text blocks make a multi-line query readable
 * without escaping.
 *
 * <p><b>The queries here are schema-independent on purpose.</b> They use GraphQL
 * introspection ({@code __schema}, {@code __type}), which every spec-compliant server
 * exposes, so these tests demonstrate the full mechanism — documents, variables, operation
 * names, error handling — and will pass against any GraphQL endpoint without depending on
 * a particular schema being live.
 *
 * <p>{@link #DISCOVER_QUERY_FIELDS} is the practical one: run it against your endpoint and
 * it lists every query the server actually offers, which is how you find the real field
 * names to write domain queries against.
 */
public final class GraphQLQueries {

    /** The smallest valid query there is — useful as a connectivity check. */
    public static final String SCHEMA_QUERY_TYPE = """
            {
              __schema {
                queryType {
                  name
                }
              }
            }
            """;

    /**
     * Lists every query the server exposes, with arguments and return types.
     *
     * <p>Run this first against a new endpoint. The {@code name} values it returns are the
     * root fields you can query, which is exactly what you need in order to write a real
     * domain query.
     */
    public static final String DISCOVER_QUERY_FIELDS = """
            {
              __schema {
                queryType {
                  name
                  fields {
                    name
                    args {
                      name
                      type {
                        name
                        kind
                      }
                    }
                    type {
                      name
                      kind
                    }
                  }
                }
              }
            }
            """;

    /** Lists every mutation the server exposes, or returns null if it has none. */
    public static final String DISCOVER_MUTATION_FIELDS = """
            {
              __schema {
                mutationType {
                  name
                  fields {
                    name
                  }
                }
              }
            }
            """;

    /**
     * A parameterised query — the variables mechanism.
     *
     * <p>Read it in three parts:
     * <ul>
     *   <li>{@code query TypeInfo(...)} names the operation, which is what
     *       {@code operationName} in the request body refers to</li>
     *   <li>{@code $typeName: String!} declares a variable; the {@code !} makes it
     *       mandatory, so the server rejects the request if it is missing</li>
     *   <li>{@code name: $typeName} substitutes the value the server-side, from the
     *       {@code variables} object — the document text itself never changes</li>
     * </ul>
     * That is why variables beat string concatenation: the server type-checks the value and
     * a quote in the data cannot break the query.
     */
    public static final String TYPE_INFO_BY_NAME = """
            query TypeInfo($typeName: String!) {
              __type(name: $typeName) {
                name
                kind
                description
              }
            }
            """;

    /**
     * Deliberately invalid — asks for a field that no schema defines.
     *
     * <p>The server answers HTTP 200 with a populated {@code errors} array and null
     * {@code data}. This is the case a status-code-only assertion lets through, and the
     * reason {@code GraphQLService} checks the envelope.
     */
    public static final String INVALID_FIELD = """
            {
              __schema {
                thisFieldDoesNotExistAnywhere
              }
            }
            """;

    /** Syntactically broken — an unclosed brace. Rejected before execution begins. */
    public static final String MALFORMED_DOCUMENT = "{ __schema { queryType { name ";

    private GraphQLQueries() {
        // constants only
    }
}
