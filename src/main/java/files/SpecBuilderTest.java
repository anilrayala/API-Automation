package files;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import pojo.AddPlace;
import pojo.Location;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/*
    * This class demonstrates how to use RequestSpecBuilder and ResponseSpecBuilder
    * from the RestAssured library to create reusable specifications for API requests
    * and responses. It also shows how to serialize a POJO into JSON for the request body.
    * Make sure to have the necessary POJO classes (AddPlace and Location) defined in the 'pojo' package.
    * Ensure that the RestAssured library is included in your project dependencies.
    * Run this class to see how the specifications work in practice.
 */
public class SpecBuilderTest {

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

        /*
            Building Request and Response Specifications
         */
        RequestSpecification req = new RequestSpecBuilder()
                .setBaseUri("https://rahulshettyacademy.com")
                .addQueryParam("key", "qaclick123")
                .setContentType(ContentType.JSON)
                .build();

        ResponseSpecification res = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();

        /*
            Using the specifications in the request
         */
        Response response = given().log().all()
                .spec(req)
                .body(addPlace)
            .when()
                .post("/maps/api/place/add/json")
            .then()
                .spec(res)
                .extract().response();

        System.out.println(response.asString());
    }
}
