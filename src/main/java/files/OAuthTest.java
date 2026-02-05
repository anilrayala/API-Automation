package files;

import io.restassured.path.json.JsonPath;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static io.restassured.RestAssured.given;

public class OAuthTest {

    public static void main(String[] args) throws InterruptedException {

//        WebDriver driver = new ChromeDriver();
//        driver.get("https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.email&auth_url=https://accounts.google.com/o/oauth2/v2/auth&client_id=692183103107-p0m7ent2hk7suguv4vq22hjcfhcr43pj.apps.googleusercontent.com&response_type=code&redirect_uri=https://rahulshettyacademy.com/getCourse.php&state=verifyfjdsss");
//        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("emailId");
//        driver.findElement(By.cssSelector("input[type='email']")).sendKeys(Keys.ENTER);
//        Thread.sleep(3000);
//
//        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password");
//        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(Keys.ENTER);
//        Thread.sleep(4000);
//
//        String url = driver.getCurrentUrl();
//        System.out.println(url);

        String url = "https://rahulshettyacademy.com/getCourse.php?state=verifyfjdsss&code=4%2F0ASc3gC2aJaKI3LeU4ICnSNdfdDKjQlk9Yoh4CQQUsQeBhRgtZl5btdhZCDMS8aCwe2APUw&scope=email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+openid&authuser=0&prompt=consent";

        String partialCode = null;
        if (url != null) {
            partialCode = url.split("code=")[1];
        }
        else System.out.println("URL is null");
        String code = partialCode.split("&scope")[0];

        System.out.println(code);

        String accessTokenResponse = given().urlEncodingEnabled(false).queryParams("code", code)
                .queryParams("client_id", "692183103107-p0m7ent2hk7suguv4vq22hjcfhcr43pj.apps.googleusercontent.com")
                .queryParams("client_secret", "erZOWM9g3UtwNRj340YYaK_W")
                .queryParams("redirect_uri", "https://rahulshettyacademy.com/getCourse.php")
                .queryParams("grant_type", "authorization_code")
                .when().log().all().post("https://www.googleapis.com/oauth2/v4/token")
                .asString();
        JsonPath js = new JsonPath(accessTokenResponse);
        String accessToken = js.getString("access_token");

        String response = given().queryParam("access_token", accessToken)
                .when().log().all().get("https://rahulshettyacademy.com/getCourse.php")
                .asString();

        System.out.println(response);

    }

}
