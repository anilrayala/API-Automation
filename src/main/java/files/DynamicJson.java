package files;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;



public class DynamicJson {

    @Test
    public void addBook(){

        RestAssured.baseURI = "http://216.10.245.166";
        String response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .body(Payload.AddBook("abc", "12345"))
        .when()
                .post("/Library/Addbook.php")
        .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = ReusableMethods.rawToJson(response);
        String bookID = jsonPath.getString("ID");
        System.out.println("Book ID is: " + bookID);

    }
}
