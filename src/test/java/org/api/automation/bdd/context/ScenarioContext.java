package org.api.automation.bdd.context;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

/**
 * State carried between the steps of a single scenario.
 *
 * <h2>The problem this replaces</h2>
 * The old {@code StepDefinition} held {@code static String placeId} and shared it with
 * {@code Hooks}, which instantiated a step-definition object with {@code new
 * StepDefinition()} in order to reach it. Three things were wrong with that:
 *
 * <ul>
 *   <li><b>Leakage between scenarios.</b> A {@code static} field keeps its value after a
 *       scenario ends, so the Delete Place scenario operated on whatever id the last Add
 *       Place scenario happened to leave behind. It looked like it worked because the
 *       scenarios ran in that order.</li>
 *   <li><b>No parallel execution.</b> Two scenarios on two threads overwrite one field.</li>
 *   <li><b>Hooks constructing step definitions.</b> Cucumber owns those objects. A
 *       hand-made instance is not the one Cucumber runs steps against, so any state it sets
 *       on itself is invisible to the scenario.</li>
 * </ul>
 *
 * <h2>How it is wired instead</h2>
 * {@code cucumber-picocontainer} is on the classpath, so Cucumber constructs step
 * definition classes through PicoContainer. Any class that declares
 * {@code ScenarioContext} as a constructor parameter receives <b>the same instance</b> for
 * the duration of one scenario, and a <b>fresh one</b> for the next. That gives sharing
 * between step classes and isolation between scenarios at the same time, with no static
 * state anywhere.
 */
public class ScenarioContext {

    /** The spec being assembled by the Given steps, handed to the When step. */
    private RequestSpecification request;

    /** The response from the most recent When step, asserted by the Then steps. */
    private Response response;

    /** Free-form values a scenario needs to carry forward, e.g. a created place id. */
    private final Map<String, Object> values = new HashMap<>();

    public RequestSpecification getRequest() {
        if (request == null) {
            throw new IllegalStateException(
                    "No request has been prepared. A scenario must run a Given step that builds "
                            + "the payload before the step that calls the API.");
        }
        return request;
    }

    public void setRequest(RequestSpecification request) {
        this.request = request;
    }

    public Response getResponse() {
        if (response == null) {
            throw new IllegalStateException(
                    "No response is available. A scenario must call the API before asserting on it.");
        }
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void put(String key, Object value) {
        values.put(key, value);
    }

    /** Reads a stored value, failing with a useful message rather than returning null. */
    public <T> T get(String key, Class<T> type) {
        Object value = values.get(key);
        if (value == null) {
            throw new IllegalStateException(
                    "'" + key + "' was never stored in this scenario's context. "
                            + "Available keys: " + values.keySet());
        }
        return type.cast(value);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }
}
