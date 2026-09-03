package org.api.automation.tests.auth;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.api.automation.base.BaseTest;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.constants.Paths;
import org.api.automation.models.response.Course;
import org.api.automation.models.response.CourseDetailsResponse;
import org.api.automation.models.response.OAuthTokenResponse;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * OAuth 2.0: acquiring a token, then using it on a protected resource.
 *
 * <p>The token is fetched once in {@code @BeforeClass} and reused across all tests in
 * this class — one network round-trip per class, not one per test.
 *
 * <p>Tagged {@code requires-credentials}: set {@code OAUTH_CLIENTID} and
 * {@code OAUTH_CLIENTSECRET} as environment variables.
 */
public class OAuthTest extends BaseTest {

    /** Fetched once per class on first use, reused by subsequent tests. */
    private String cachedToken;

    private String token() {
        if (cachedToken == null) {
            OAuthTokenResponse tokenResponse = RestAssured.given()
                    .baseUri(ConfigManager.get(ConfigKeys.OAUTH_BASE_URL))
                    .contentType(ContentType.URLENC)
                    .filter(new RequestLoggingFilter(trafficLog()))
                    .filter(new ResponseLoggingFilter(trafficLog()))
                    .formParam("client_id", ConfigManager.getRequired(ConfigKeys.OAUTH_CLIENT_ID))
                    .formParam("client_secret", ConfigManager.getRequired(ConfigKeys.OAUTH_CLIENT_SECRET))
                    .formParam("grant_type", "client_credentials")
                    .formParam("scope", ConfigManager.get(ConfigKeys.OAUTH_SCOPE, "trust"))
                    .post(Paths.OAUTH_TOKEN)
                    .then().extract().as(OAuthTokenResponse.class);

            assertThat("Access token", tokenResponse.getAccessToken(), not(emptyOrNullString()));
            cachedToken = tokenResponse.getAccessToken();
            log.info("Token acquired: {}", tokenResponse);
        }
        return cachedToken;
    }

    /** Client-credentials grant: id + secret in, access token out, one call. */
    @Test(groups = {"auth", "requires-credentials"})
    public void shouldObtainAccessTokenWithClientCredentials() {
        OAuthTokenResponse fresh = RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.OAUTH_BASE_URL))
                .contentType(ContentType.URLENC)
                .filter(new RequestLoggingFilter(trafficLog()))
                .filter(new ResponseLoggingFilter(trafficLog()))
                .formParam("client_id", ConfigManager.getRequired(ConfigKeys.OAUTH_CLIENT_ID))
                .formParam("client_secret", ConfigManager.getRequired(ConfigKeys.OAUTH_CLIENT_SECRET))
                .formParam("grant_type", "client_credentials")
                .formParam("scope", ConfigManager.get(ConfigKeys.OAUTH_SCOPE, "trust"))
                .post(Paths.OAUTH_TOKEN)
                .then().extract().as(OAuthTokenResponse.class);

        assertThat("Access token", fresh.getAccessToken(), not(emptyOrNullString()));
        assertNotNull(fresh.getExpiresIn(), "Token lifetime should be reported");
        assertThat("Token lifetime should be positive", fresh.getExpiresIn(), greaterThan(0L));
        log.info("Received {}", fresh);
    }

    /** Uses the token on the protected resource and deserializes the nested response. */
    @Test(groups = {"auth", "requires-credentials"})
    public void shouldAccessProtectedResourceWithToken() {
        CourseDetailsResponse details = RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.OAUTH_BASE_URL))
                .contentType(ContentType.JSON)
                .queryParam("access_token", token())
                .filter(new RequestLoggingFilter(trafficLog()))
                .filter(new ResponseLoggingFilter(trafficLog()))
                .get(Paths.OAUTH_COURSE_DETAILS)
                .then().extract().as(CourseDetailsResponse.class);

        assertEquals(details.getInstructor(), "RahulShetty", "Instructor");
        assertThat("LinkedIn URL", details.getLinkedIn(), not(emptyOrNullString()));
        assertNotNull(details.getCourses(), "Nested course catalog");

        List<String> webTitles = details.webAutomationTitles();
        assertThat("Web automation catalogue", webTitles, hasItem("Selenium Webdriver Java"));
    }

    /** Conditional lookup over the deserialized tree — Optional makes a missing course visible. */
    @Test(groups = {"auth", "requires-credentials"})
    public void shouldFindSpecificCourseInProtectedResource() {
        CourseDetailsResponse details = RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.OAUTH_BASE_URL))
                .contentType(ContentType.JSON)
                .queryParam("access_token", token())
                .filter(new RequestLoggingFilter(trafficLog()))
                .filter(new ResponseLoggingFilter(trafficLog()))
                .get(Paths.OAUTH_COURSE_DETAILS)
                .then().extract().as(CourseDetailsResponse.class);

        Optional<Course> course = details.findCourseByTitle("SoapUI Webservices testing");

        assertTrue(course.isPresent(), "SoapUI course should be in the catalog");
        assertEquals(course.get().getPrice(), "40", "SoapUI course price");
    }

    // ------------------------------------------------------------------
    // Offline tests — pure string parsing, no network required
    // ------------------------------------------------------------------

    /**
     * Extracts the {@code code} parameter from an OAuth redirect URL.
     *
     * <p>The URL must be parsed properly — not split on {@code "code="} and {@code "&scope"} —
     * because providers do not guarantee parameter order.
     */
    @Test(groups = {"auth", "offline"})
    public void shouldExtractAuthorizationCodeFromRedirectUrl() {
        String redirectUrl = "https://rahulshettyacademy.com/getCourse.php"
                + "?state=verifyfjdsss"
                + "&code=4%2F0ASc3gC2aJaKI3LeU4ICnSNdfdDKjQlk9Yoh4CQQUsQeBhRgtZl5btdhZCDMS8aCwe2APUw"
                + "&scope=email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+openid"
                + "&authuser=0&prompt=consent";

        String code = extractAuthorizationCode(redirectUrl);

        assertEquals(code,
                "4%2F0ASc3gC2aJaKI3LeU4ICnSNdfdDKjQlk9Yoh4CQQUsQeBhRgtZl5btdhZCDMS8aCwe2APUw",
                "Code must come back exactly as it appeared, still percent-encoded");
    }

    @Test(groups = {"auth", "offline"})
    public void shouldExtractCodeRegardlessOfParameterOrder() {
        assertEquals(extractAuthorizationCode("https://host/cb?code=abc123&state=xyz"), "abc123");
        assertEquals(extractAuthorizationCode("https://host/cb?state=xyz&code=abc123"), "abc123");
        assertEquals(extractAuthorizationCode("https://host/cb?a=1&code=abc123&scope=email&b=2"), "abc123");

        assertThrows(IllegalArgumentException.class,
                () -> extractAuthorizationCode("https://host/cb?state=xyz"));
    }

    /**
     * Pulls the {@code code} value out of an OAuth redirect URL.
     *
     * <p>Proper URL query-string parsing — no split chains, no order assumptions.
     * The old code used {@code url.split("code=")[1].split("&scope")[0]}, which broke when
     * {@code scope} was not the next parameter.
     */
    private static String extractAuthorizationCode(String redirectUrl) {
        if (redirectUrl == null || !redirectUrl.contains("?")) {
            throw new IllegalArgumentException("Not a redirect URL with a query string: " + redirectUrl);
        }
        String query = redirectUrl.substring(redirectUrl.indexOf('?') + 1);
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && "code".equals(pair.substring(0, eq))) {
                return pair.substring(eq + 1);
            }
        }
        throw new IllegalArgumentException("No 'code' parameter present in: " + redirectUrl);
    }
}
