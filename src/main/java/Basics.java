import files.Payload;
import files.ReusableMethods;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class Basics {

    public static void main(String[] args){

        /*
        3 main parts of REST Assured
         1. Given - all input details
                - baseURI
                - Query Parameters
                - Headers
                - Body (Payload)
         2. When - Submit the API (HTTP Method + Endpoint)
         3. Then - Validate the response
                - Status Code
                - Headers
                - Response Time
                - Body (Response Payload)
         */

        RestAssured.baseURI = "https://rahulshettyacademy.com";

        /*
        Add Place API
         */
        String response = given()
                .log().all()
                .queryParam("key", "qaclick123")
                .header("Content-Type", "application/json")
                .body(Payload.AddPlace())
        .when()
                .post("/maps/api/place/add/json")
        .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("scope", equalTo("APP"))
                .header("Server", "Apache/2.4.52 (Ubuntu)").extract().response().asString();
        System.out.println("Response is: " + response);

        /*
        Parse the response to get the place_id
        JsonPath class is used to parse the JSON response
        it takes the response string as input
        it provides methods to extract values from the JSON response
         */
        JsonPath js = ReusableMethods.rawToJson(response);
        String placeId = js.getString("place_id");
        System.out.println("Place ID is: " + placeId);

        /*
        Update Place API
         */
        given()
                .log().all()
                .queryParam("key", "qaclick123")
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "\"place_id\":\""+placeId+"\",\n" +
                        "\"address\":\"70 Summer walk, USA\",\n" +
                        "\"key\":\"qaclick123\"\n" +
                        "}").
        when().put("/maps/api/place/update/json").
        then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("msg", equalTo("Address successfully updated"));
        /*
        Get Place API
         */
        String getPlaceResponse = given()
                .log().all()
                .queryParam("key", "qaclick123")
                .queryParam("place_id", placeId).
        when().get("/maps/api/place/get/json").
        then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();
        System.out.println("Get Place Response is: " + getPlaceResponse);
        /*
        Parse the get place response to get the address
         */
        JsonPath js1 = ReusableMethods.rawToJson(getPlaceResponse);
        String actualAddress = js1.getString("address");
        System.out.println("Actual Address is: " + actualAddress);
        /*
        Validate if the updated address is correct
         */
        Assert.assertEquals(actualAddress, "70 Summer walk, USA");

    }

}
