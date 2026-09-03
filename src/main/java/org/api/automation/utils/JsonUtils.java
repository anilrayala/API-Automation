package org.api.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * JsonPath extraction and Jackson serialization helpers.
 *
 * <p>The old code created {@code new JsonPath(response.asString())} at nearly every call
 * site — sometimes via {@code ReusableMethods.rawToJson}, sometimes inline, sometimes as
 * {@code js}, {@code js1}, {@code js2}, {@code js3}. This gathers those into typed
 * accessors so a test reads {@code JsonUtils.getString(response, "place_id")} rather than
 * three lines of parsing.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtils() {
        // static utility
    }

    /**
     * Parses a raw JSON string into a {@link JsonPath}.
     *
     * <p>Useful when the JSON did not come from an HTTP call — for example a payload held
     * as a string constant, which is how the complex-JSON parsing exercises work.
     */
    public static JsonPath toJsonPath(String rawJson) {
        return new JsonPath(rawJson);
    }

    /** Parses a response body into a {@link JsonPath} for repeated extraction. */
    public static JsonPath toJsonPath(Response response) {
        return response.jsonPath();
    }

    /** Extracts a String at {@code jsonPath}, e.g. {@code "place_id"} or {@code "courses[0].title"}. */
    public static String getString(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    /** Extracts an int at {@code jsonPath}, e.g. {@code "dashboard.purchaseAmount"}. */
    public static int getInt(Response response, String jsonPath) {
        return response.jsonPath().getInt(jsonPath);
    }

    /** Extracts a list at {@code jsonPath}, e.g. {@code "courses.title"} for every title. */
    public static <T> java.util.List<T> getList(Response response, String jsonPath) {
        return response.jsonPath().getList(jsonPath);
    }

    /**
     * Size of the array at {@code jsonPath}.
     *
     * <p>Groovy's GPath accepts {@code "courses.size()"}; this wraps it so the intent is
     * obvious at the call site.
     */
    public static int size(Response response, String jsonPath) {
        return response.jsonPath().getInt(jsonPath + ".size()");
    }

    /** Serializes any object to a JSON string — handy for logging a payload before sending. */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize " + object.getClass().getSimpleName() + " to JSON", e);
        }
    }

    /** Deserializes a JSON string into {@code type}. */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize JSON into " + type.getSimpleName(), e);
        }
    }
}
