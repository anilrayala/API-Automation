package files;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class ExcelTest {

    //@Test
    public void addBook() throws IOException {

        ArrayList<String> data = ExcelReader.getData("TestData", "TestCases", "AddBook");

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", data.get(1));
        map.put("author", data.get(2));
        map.put("isbn", data.get(3));
        map.put("aisle", data.get(4));

        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("latitude", "-38.383494");
        map2.put("longitude", "33.427362");

        /*
        this way we can access the nested JSON object and add it to the main JSON object.
        We can also create a separate class for the nested JSON object and use it as a field in the main class.
        This way we can keep our code clean and organized.
         */
        map.put("location", map2);

        RestAssured.baseURI = "https://rahulshettyacademy.com";
        Response res = given().header("Content-Type","application/json")
                .body(map)
                .when().post("/Library/Addbook.php")
                .then().assertThat().statusCode(200).extract().response();
        String response = res.asString();

        JsonPath js = ReusableMethods.rawToJson(response);
        String id = js.get("ID");
        System.out.println(id);



    }

}
