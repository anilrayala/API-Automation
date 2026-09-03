package org.api.automation.bdd.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.api.automation.base.BaseTest;
import org.api.automation.bdd.context.ScenarioContext;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.DeletePlaceRequest;
import org.api.automation.utils.JsonUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Step definitions for the Places feature.
 *
 * <p>PicoContainer injects {@link ScenarioContext} via the constructor — every class in the
 * same scenario that declares this parameter receives the same instance. No static state,
 * no inheritance, no shared fields between scenarios.
 */
public class PlaceStepDefinitions {

    private final ScenarioContext context;

    public PlaceStepDefinitions(ScenarioContext context) {
        this.context = context;
    }

    // ------------------------------------------------------------------
    // Given — build the request
    // ------------------------------------------------------------------

    @Given("Add Place Payload with {string} {string} {string}")
    public void addPlacePayloadWith(String name, String language, String address) {
        context.setRequest(
                placesSpec().body(TestDataFactory.addPlace(name, language, address)));
        context.put("submittedName", name);
        context.put("submittedAddress", address);
    }

    @Given("Delete Place Payload with the created place_Id")
    public void deletePlacePayloadWithCreatedPlaceId() {
        String placeId = context.getString("placeId");
        context.setRequest(placesSpec().body(new DeletePlaceRequest(placeId)));
    }

    // ------------------------------------------------------------------
    // When — fire the call
    // ------------------------------------------------------------------

    /**
     * Fires any verb at any endpoint, both named as strings from the feature file.
     *
     * <p>The simple switch replaces the old if/else ladder and the {@code HttpMethod} enum
     * with BiFunction dispatcher. Fewer moving parts, same result.
     */
    @When("User calls {string} API with {string} http request")
    public void userCallsApiWithHttpRequest(String endpoint, String method) {
        String path = Paths.fromName(endpoint);
        RequestSpecification spec = context.getRequest();

        Response response = switch (method.toUpperCase()) {
            case "GET"    -> spec.get(path);
            case "POST"   -> spec.post(path);
            case "PUT"    -> spec.put(path);
            case "DELETE" -> spec.delete(path);
            default -> throw new IllegalArgumentException("Unknown HTTP method: " + method);
        };
        context.setResponse(response);
    }

    // ------------------------------------------------------------------
    // Then — assert
    // ------------------------------------------------------------------

    @Then("API call is successful with status code {int}")
    public void apiCallIsSuccessfulWithStatusCode(int expectedStatusCode) {
        assertEquals(context.getResponse().getStatusCode(), expectedStatusCode, "Status code");
    }

    @And("{string} in response body is {string}")
    public void keyInResponseBodyIs(String jsonPath, String expectedValue) {
        assertEquals(JsonUtils.getString(context.getResponse(), jsonPath), expectedValue,
                "Response field '" + jsonPath + "'");
    }

    @Then("place_Id is captured from the response")
    public void placeIdIsCapturedFromTheResponse() {
        String placeId = JsonUtils.getString(context.getResponse(), "place_id");
        assertNotNull(placeId, "place_id must be present in the Add Place response");
        context.put("placeId", placeId);
    }

    @Then("the created place can be retrieved with name {string}")
    public void theCreatedPlaceCanBeRetrievedWithName(String expectedName) {
        String placeId = context.getString("placeId");

        context.setRequest(placesSpec().queryParam("place_id", placeId));
        userCallsApiWithHttpRequest("GET_PLACE", "GET");

        apiCallIsSuccessfulWithStatusCode(200);
        assertEquals(JsonUtils.getString(context.getResponse(), "name"), expectedName,
                "Name of the retrieved place");
    }

    @Then("the created place has address {string}")
    public void theCreatedPlaceHasAddress(String expectedAddress) {
        assertEquals(JsonUtils.getString(context.getResponse(), "address"), expectedAddress,
                "Address of the retrieved place");
    }

    // ------------------------------------------------------------------
    // Spec builder — fresh per step, not shared
    // ------------------------------------------------------------------

    private RequestSpecification placesSpec() {
        return RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.PLACES_BASE_URL))
                .queryParam("key", ConfigManager.get(ConfigKeys.PLACES_API_KEY))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(BaseTest.trafficLog()))
                .filter(new ResponseLoggingFilter(BaseTest.trafficLog()));
    }
}
