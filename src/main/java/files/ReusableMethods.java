package files;

import io.restassured.path.json.JsonPath;

public class ReusableMethods {

    /*
    This method converts raw response string to JsonPath object
    so that we can extract values from it
     */
    public static JsonPath rawToJson(String response){
        return new JsonPath(response);
    }
}
