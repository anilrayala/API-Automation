package stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import resources.APIResources;
import resources.TestDataBuild;
import resources.Utils;

import java.io.FileNotFoundException;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class StepDefinition extends Utils {

    RequestSpecification req;
    ResponseSpecification res;
    Response response;

    TestDataBuild data = new TestDataBuild();
    /*
    static variable to hold placeId across different step definitions
     */
    static String placeId;

    @Given("Add Place Payload with {string} {string} {string}")
    public void addPlacePayloadWith(String name, String language, String address) throws FileNotFoundException {
        // prepare request spec with body here
        req = requestSpecification().body(data.addPlacePayload(name, language, address));
    }

    @When("User calls {string} API with {string} http request")
    public void user_calls_api_with_http_request(String resource, String methodName) throws FileNotFoundException {

        // ensure response spec is initialized for every flow to avoid NPE
        res = responseSpecification();

        if (methodName.equalsIgnoreCase("GET")) {
            response = given().log().all()
                    .spec(req)
                    .when()
                    .get(APIResources.valueOf(resource).getResource());
        } else if (methodName.equalsIgnoreCase("POST")) {
            response = given().log().all()
                    .spec(req)
                    .when()
                    .post(APIResources.valueOf(resource).getResource());
        } else if (methodName.equalsIgnoreCase("PUT")) {
            response = given().log().all()
                    .spec(req)
                    .when()
                    .put(APIResources.valueOf(resource).getResource());
        } else if (methodName.equalsIgnoreCase("DELETE")) {
            response = given().log().all()
                    .spec(req)
                    .when()
                    .delete(APIResources.valueOf(resource).getResource());
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + methodName);
        }
    }

    @Then("API call is successful with status code {int}")
    public void api_call_is_successful_with_status_code(int statusCode) {
        // validate response against the expected response spec and status code in one step
        response = response.then().spec(res).extract().response();
        assertEquals(statusCode, response.getStatusCode());
    }

    @And("{string} in response body is {string}")
    public void key_in_response_body_is(String key, String expectedValue) {
        // use RestAssured's JsonPath directly from the response
        String actualValue = getJsonPath(response, key);
        assertEquals(expectedValue, actualValue);
    }

    @Then("verify place_Id created maps to {string} using {string} API")
    public void verify_place_id_created_maps_to_using(String expectedName, String resource) throws FileNotFoundException {
        // Extract place_id from the previous response
        placeId = getJsonPath(response, "place_id");
        // Prepare request spec for GET request with place_id as query parameter
        req = requestSpecification().queryParam("place_id", placeId);
        // Call the GET API
        user_calls_api_with_http_request(resource, "GET");
        // Validate the name in the response matches the expected name
        String name = getJsonPath(response, "name");
        assertEquals(expectedName, name);
    }

    @Given("Delete Place Payload with place_Id")
    public void deletePlacePayloadWithPlace_Id() throws FileNotFoundException {
        // Write code here that turns the phrase above into concrete actions
        req  = requestSpecification().body(data.deletePlacePayload(placeId));
    }
}
