package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * The envelope every GraphQL response arrives in.
 *
 * <p>This is the single most important difference from REST testing. A GraphQL server
 * answers {@code 200 OK} even when the operation failed — a bad field name, a validation
 * error, an unauthorised query all come back as 200 with an {@code errors} array and a
 * {@code data} that is null or partial.
 *
 * <p>So {@code assertEquals(200, statusCode)} proves almost nothing here. A GraphQL
 * assertion has to check {@link #hasErrors()} as well, which is what
 * {@code GraphQLService} enforces.
 *
 * <p>{@code data} is a {@code Map} rather than a typed model because its shape is defined
 * by the query the test just wrote — there is no one fixed response type for an endpoint.
 * Tests that want type safety deserialize the {@code data} sub-tree into their own model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse {

    private Map<String, Object> data;
    private List<GraphQLError> errors;

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public void setErrors(List<GraphQLError> errors) {
        this.errors = errors;
    }

    /** True when the server reported at least one error, regardless of HTTP status. */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /** All error messages joined, for use in an assertion failure message. */
    public String errorMessages() {
        if (!hasErrors()) {
            return "";
        }
        return errors.stream().map(GraphQLError::getMessage).reduce((a, b) -> a + "; " + b).orElse("");
    }

    /** A single entry in the {@code errors} array. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphQLError {

        private String message;
        private List<Map<String, Object>> locations;
        private List<Object> path;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Map<String, Object>> getLocations() {
            return locations;
        }

        public void setLocations(List<Map<String, Object>> locations) {
            this.locations = locations;
        }

        public List<Object> getPath() {
            return path;
        }

        public void setPath(List<Object> path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
