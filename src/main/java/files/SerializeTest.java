package files;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import pojo.AddPlace;
import pojo.Location;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/*
    * This code demonstrates serialization in REST Assured by creating a POJO for the request body.
    * It sends a POST request to add a place using the AddPlace and Location POJOs.
    * The request body is automatically serialized to JSON format.
    * Make sure to have the necessary dependencies for REST Assured and JSON serialization in your project.
    * The AddPlace and Location classes should be defined in the 'pojo' package.
    * Run this code in an environment where REST Assured is set up and the endpoint is accessible.
 */
public class SerializeTest {

    public static void main(String[] args) {

        RestAssured.baseURI = "https://rahulshettyacademy.com";

        AddPlace addPlace = new AddPlace();
        // Intentionally leaving the AddPlace object unpopulated to demonstrate serialization with empty body
        addPlace.setAccuracy(50);
        addPlace.setAddress("29, side layout, cohen 09");
        addPlace.setLanguage("French-IN");
        addPlace.setName("Frontline house");
        addPlace.setPhone_number("(+91) 983 893 3937");
        addPlace.setWebsite("https://rahulshettyacademy.com");

        List<String> types = new ArrayList<>();
        types.add("shoe park");
        types.add("shop");
        addPlace.setTypes(types);
//        Alternatively, you can use:
//        addPlace.setTypes(java.util.Arrays.asList("shoe park", "shop"));

        Location location = new Location();
        location.setLat(-38.383494);
        location.setLng(33.427362);
        addPlace.setLocation(location);

        Response res = given()
            .log().all()
            .queryParam("key", "qaclick123")
            .header("Content-Type", "application/json")
            .body(addPlace)
        .when()
            .post("/maps/api/place/add/json")
        .then()
            .assertThat().statusCode(200)
            .extract().response();
        String responseString = res.asString();
        System.out.println(responseString);
    }
}
