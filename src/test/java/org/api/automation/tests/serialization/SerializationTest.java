package org.api.automation.tests.serialization;

import io.restassured.path.json.JsonPath;
import org.api.automation.base.BaseTest;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.AddPlaceRequest;
import org.api.automation.models.response.Course;
import org.api.automation.models.response.CourseDetailsResponse;
import org.api.automation.utils.JsonUtils;
import org.api.automation.utils.ResourceReader;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Serialization and deserialization, tested without touching the network.
 *
 * <p>Worth having as its own suite. When a POJO-based API test fails, the question is
 * whether the mapping is wrong or the API is. These tests answer the first half offline, so
 * a failure here points squarely at the models.
 *
 * <ul>
 *   <li><b>Serialization</b> — Java object to JSON. Used for request bodies.</li>
 *   <li><b>Deserialization</b> — JSON to Java object. Used for response bodies.</li>
 * </ul>
 */
public class SerializationTest extends BaseTest {

    /**
     * Serialization, including the snake_case mapping.
     *
     * <p>The specific thing under test is {@code @JsonProperty("phone_number")}: the Java
     * field is {@code phoneNumber}, and the emitted JSON must be {@code phone_number} or the
     * API silently ignores the value. Asserting that the camelCase key is absent is the half
     * people leave out — without it, a mapping that emits *both* keys would pass.
     */
    @Test(groups = {"offline", "smoke"})
    public void shouldSerializePojoToApiFieldNames() {
        AddPlaceRequest request = TestDataFactory.addPlace("Frontline House", "English", "123 Main St");

        String json = JsonUtils.toJson(request);
        JsonPath parsed = JsonUtils.toJsonPath(json);

        assertEquals(parsed.getString("name"), "Frontline House");
        assertEquals(parsed.getString("phone_number"), "(+91) 983 893 3937",
                "Field must serialize as snake_case for this API");
        assertNull(parsed.get("phoneNumber"), "camelCase form must not be emitted");

        // Nested object and list both come through structurally.
        //
        // The delta is 1e-4, not 1e-6, and that is not laziness. JsonPath coerces JSON
        // numbers through float, whose nearest representable value to -38.383494 is about
        // -38.3834953 — an error of exactly 1e-6, which a 1e-6 delta rejects because the
        // comparison is strict. Never assert exact equality on a float or double that has
        // been through a JSON round-trip; pick a delta comfortably larger than the type's
        // precision at that magnitude.
        assertEquals(parsed.getDouble("location.lat"), -38.383494, 0.0001);
        assertThat(parsed.getList("types"), contains("shoe park", "shop"));
        assertEquals(parsed.getInt("accuracy"), 50);
    }

    /**
     * Deserialization of a three-level nested response.
     *
     * <p>{@link CourseDetailsResponse} holds a catalog, which holds three lists of
     * {@link Course}. One call builds the whole tree.
     */
    @Test(groups = {"offline", "smoke"})
    public void shouldDeserializeNestedResponseIntoPojoTree() {
        String json = ResourceReader.readAsString("testdata/oauth-course-details-sample.json");

        CourseDetailsResponse response = JsonUtils.fromJson(json, CourseDetailsResponse.class);

        assertEquals(response.getInstructor(), "RahulShetty");
        assertEquals(response.getExpertise(), "Automation");
        assertThat("Nested catalog must be populated", response.getCourses(), notNullValue());

        // Each of the three arrays binds to the same Course type
        assertEquals(response.getCourses().getWebAutomation().size(), 3, "Web automation courses");
        assertEquals(response.getCourses().getApi().size(), 2, "API courses");
        assertEquals(response.getCourses().getMobile().size(), 1, "Mobile courses");
        assertEquals(response.getCourses().all().size(), 6, "Total courses");

        // Field access is compile-checked, unlike a JsonPath string
        assertEquals(response.getCourses().getWebAutomation().get(0).getCourseTitle(),
                "Selenium Webdriver Java");
        assertEquals(response.getCourses().getApi().get(1).getPrice(), "40");
    }

    /**
     * The typed equivalent of a conditional JsonPath lookup.
     *
     * <p>Once the response is an object graph, "find the course titled X" is an ordinary
     * stream filter, and it returns {@link Optional} so a missing course produces a clear
     * assertion failure rather than a {@code NullPointerException} three lines later.
     */
    @Test(groups = {"offline"})
    public void shouldFindCourseByTitleInDeserializedTree() {
        CourseDetailsResponse response = JsonUtils.fromJson(
                ResourceReader.readAsString("testdata/oauth-course-details-sample.json"),
                CourseDetailsResponse.class);

        Optional<Course> soapUi = response.findCourseByTitle("SoapUI Webservices testing");

        assertTrue(soapUi.isPresent(), "SoapUI course should exist in the catalog");
        assertEquals(soapUi.get().priceAsInt(), 40, "SoapUI course price");

        assertTrue(response.findCourseByTitle("Course That Does Not Exist").isEmpty(),
                "Unknown course must resolve to an empty Optional, not null");
    }

    /** Ordered list assertion against the deserialized tree. */
    @Test(groups = {"offline"})
    public void webAutomationTitlesShouldMatchExpectedCatalogue() {
        CourseDetailsResponse response = JsonUtils.fromJson(
                ResourceReader.readAsString("testdata/oauth-course-details-sample.json"),
                CourseDetailsResponse.class);

        List<String> titles = response.webAutomationTitles();

        assertThat("Web automation catalogue, in response order", titles,
                contains("Selenium Webdriver Java", "Cypress", "Protractor"));
    }

    /**
     * Unknown fields must not break deserialization.
     *
     * <p>This is what {@code @JsonIgnoreProperties(ignoreUnknown = true)} buys, and it is
     * the difference between a suite that survives a backend release and one that fails
     * everywhere the morning after a new field ships.
     */
    @Test(groups = {"offline"})
    public void shouldToleratePreviouslyUnknownResponseFields() {
        String jsonWithNewField = """
                {
                  "instructor": "RahulShetty",
                  "aFieldAddedByTheBackendTeamLastNight": "surprise",
                  "courses": { "webAutomation": [], "api": [], "mobile": [] }
                }
                """;

        CourseDetailsResponse response = JsonUtils.fromJson(jsonWithNewField, CourseDetailsResponse.class);

        assertEquals(response.getInstructor(), "RahulShetty",
                "Known fields must still bind when unknown ones are present");
    }
}
