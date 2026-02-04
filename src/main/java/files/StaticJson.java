package files;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
//import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;


public class StaticJson {

    //@Test
    public void addBook() throws IOException {
        RestAssured.baseURI = "http://216.10.245.166";
       String response = given().header("Content-Type", "application/json")
                .body(GenerateStringFromResource("src/main/resources/AddBookDetails.json"))
                .when().post("/Library/Addbook.php")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();
        JsonPath js = ReusableMethods.rawToJson(response);
        String id = js.get("ID");
        System.out.println(id);
    }

    //@Test
    public void deleteBook() throws IOException {
        RestAssured.baseURI = "http://216.10.245.166";
        String response = given().header("Content-Type", "application/json")
                .body(GenerateStringFromResource("src/main/resources/DeleteBookDetails.json"))
                .when().post("/Library/DeleteBook.php")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();
        JsonPath js = ReusableMethods.rawToJson(response);
        String msg = js.get("msg");
        System.out.println(msg);
    }

    public static String GenerateStringFromResource(String path) throws IOException {
        // implementation to read file from the given path and return as String
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
