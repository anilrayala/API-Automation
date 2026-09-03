package org.api.automation.base;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.listeners.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * One base URL, one content type, one log filter — in one place.
 *
 * <p>Each {@code xxxSpec()} method returns a fresh {@link RequestSpecification} ready to
 * use: just chain {@code .body(...)} or {@code .queryParam(...)} and call the verb.
 * Returning a new spec per call means there is no shared mutable state — two tests running
 * in parallel cannot corrupt each other's request.
 *
 * <p>Request and response bodies are written to {@code target/logs/api-traffic.log} via a
 * shared {@link PrintStream}. The stream itself is thread-safe (synchronised writes), so
 * parallel tests share one log file without interleaving.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    private static final PrintStream TRAFFIC_LOG = openTrafficLog();

    private static PrintStream openTrafficLog() {
        try {
            new File("target/logs").mkdirs();
            return new PrintStream(new FileOutputStream("target/logs/api-traffic.log", true));
        } catch (IOException e) {
            return System.out;
        }
    }

    @BeforeSuite(alwaysRun = true)
    public void logEnvironment() {
        LogManager.getLogger(BaseTest.class)
                .info("Running suite against environment '{}'", ConfigManager.environment());
    }

    // ------------------------------------------------------------------
    // Spec builders — one per API, each returning a fresh spec
    // ------------------------------------------------------------------

    protected RequestSpecification placesSpec() {
        return RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.PLACES_BASE_URL))
                .queryParam("key", ConfigManager.get(ConfigKeys.PLACES_API_KEY))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(TRAFFIC_LOG))
                .filter(new ResponseLoggingFilter(TRAFFIC_LOG));
    }

    protected RequestSpecification librarySpec() {
        return RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.LIBRARY_BASE_URL))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(TRAFFIC_LOG))
                .filter(new ResponseLoggingFilter(TRAFFIC_LOG));
    }

    protected RequestSpecification ecomSpec() {
        return RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.ECOM_BASE_URL))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(TRAFFIC_LOG))
                .filter(new ResponseLoggingFilter(TRAFFIC_LOG));
    }

    /** Authenticated variant — adds the token from a previous login call. */
    protected RequestSpecification ecomSpec(String token) {
        return ecomSpec().header("Authorization", token);
    }

    protected RequestSpecification graphqlSpec() {
        return RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.GRAPHQL_BASE_URL))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(TRAFFIC_LOG))
                .filter(new ResponseLoggingFilter(TRAFFIC_LOG));
    }

    /**
     * Accessible to non-BaseTest classes (e.g. Cucumber Hooks) that need to log traffic.
     * Returns the shared traffic log stream.
     */
    public static PrintStream trafficLog() {
        return TRAFFIC_LOG;
    }
}
