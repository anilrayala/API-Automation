package files;

import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import pojo.API;
import pojo.GetCourses;
import pojo.WebAutomation;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/*
    * This code demonstrates how to perform OAuth 2.0 authentication using REST Assured.
    * It retrieves an access token and uses it to access a protected resource.
    * The response is deserialized into POJOs for easier data manipulation.
    * Make sure to have the necessary dependencies for REST Assured and JSON deserialization in your project.
    * The GetCourses, API, and WebAutomation classes should be defined in the 'pojo' package.
    * Run this code in an environment where REST Assured is set up and the endpoints are accessible.
 */
public class DeserializedOAuthTest {

    public static void main(String[] args) {
       String response =  given()
                .formParams("client_id", "692183103107-p0m7ent2hk7suguv4vq22hjcfhcr43pj.apps.googleusercontent.com")
                .formParams("client_secret", "erZOWM9g3UtwNRj340YYaK_W")
                .formParams("grant_type", "client_credentials")
                .formParams("scope", "trust")
                .when().log().all().post("https://rahulshettyacademy.com/oauthapi/oauth2/resourceOwner/token").asString();

        JsonPath jsonpath = ReusableMethods.rawToJson(response);
        String accessToken = jsonpath.getString("access_token");
        System.out.println(accessToken);

        GetCourses gc = given().queryParam("access_token", accessToken)
                .when().log().all()
                .get("https://rahulshettyacademy.com/oauthapi/getCourseDetails")
                .as(GetCourses.class);

        System.out.println(gc.getCourses().getWebAutomation().get(0).getCourseTitle());
        System.out.println(gc.getCourses().getApi().get(1).getPrice());
        System.out.println(gc.getInstructor());
        System.out.println(gc.getLinkedIn());

        List<API> apiCourses = gc.getCourses().getApi();

        for (API apiCourse : apiCourses) {
            if (apiCourse.getCourseTitle().equalsIgnoreCase("SoapUI Webservices testing")) {
                System.out.println(apiCourse.getPrice());
            }
        }

        List<WebAutomation> webAutomationCourses = gc.getCourses().getWebAutomation();

        for(WebAutomation webAutomation : webAutomationCourses) {
            System.out.println(webAutomation.getCourseTitle());
        }

        String[] courseTitles = {"Selenium Webdriver Java", "Cypress", "Protractor"};

        ArrayList<String> a = new ArrayList<>();
        List<WebAutomation> webAutomations = gc.getCourses().getWebAutomation();
        for (WebAutomation webAutomation : webAutomations) {
            a.add(webAutomation.getCourseTitle());
        }

        List<String> expectedList = List.of(courseTitles);
        Assert.assertEquals(a, expectedList);
    }
}
