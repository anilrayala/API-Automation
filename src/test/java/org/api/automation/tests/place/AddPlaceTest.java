package org.api.automation.tests.place;

import io.restassured.response.Response;
import org.api.automation.base.BaseTest;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.AddPlaceRequest;
import org.api.automation.models.response.AddPlaceResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Add Place — demonstrates the two assertion styles REST Assured supports.
 *
 * <ul>
 *   <li><b>Fluent / Hamcrest</b> — chain .then().body("key", equalTo("value")).
 *       Best for asserting several things about one response in one readable block.</li>
 *   <li><b>Extract then assert</b> — pull a typed model out, then use TestNG asserts.
 *       Best when a value needs to be reused or compared across calls.</li>
 * </ul>
 */
public class AddPlaceTest extends BaseTest {

    /** Status code, body fields, and response time in one fluent chain. */
    @Test(groups = {"smoke", "regression"})
    public void shouldAddPlaceAndReturnExpectedFields() {
        given(placesSpec())
                .body(TestDataFactory.addPlace("Frontline House", "English", "123 Main St"))
                .post(Paths.ADD_PLACE)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("OK"))
                .body("scope", equalTo("APP"))
                .body("place_id", not(emptyOrNullString()))
                .time(lessThan(10_000L));
    }

    /** The same call validated through a typed model — extract then assert. */
    @Test(groups = {"smoke", "regression"})
    public void shouldReturnUsablePlaceIdInTypedResponse() {
        AddPlaceResponse response = given(placesSpec())
                .body(TestDataFactory.defaultPlace())
                .post(Paths.ADD_PLACE)
                .then()
                .statusCode(200)
                .extract()
                .as(AddPlaceResponse.class);

        assertEquals(response.getStatus(), "OK", "Status");
        assertEquals(response.getScope(), "APP", "Scope");
        assertNotNull(response.getPlaceId(), "place_id must be present");
    }

    /** Header assertion — assert shape, not exact version strings. */
    @Test(groups = {"regression"})
    public void shouldReturnJsonContentTypeHeader() {
        given(placesSpec())
                .body(TestDataFactory.defaultPlace())
                .post(Paths.ADD_PLACE)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"));
    }

    /**
     * Negative test — tagged needs-verification and excluded from the default suite.
     * Run it once, check the logged status, then move to regression or delete.
     */
    @Test(groups = {"negative", "needs-verification"})
    public void shouldRejectRequestWithMissingRequiredFields() {
        Response response = given(placesSpec())
                .body(new AddPlaceRequest())
                .post(Paths.ADD_PLACE);

        int status = response.getStatusCode();
        log.info("Empty payload returned status {} with body: {}", status, response.asString());

        assertTrue(status >= 400 && status < 500,
                "An empty payload should be rejected with a 4xx, but got " + status);
    }
}
