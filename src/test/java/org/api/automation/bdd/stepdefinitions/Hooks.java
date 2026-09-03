package org.api.automation.bdd.stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.api.automation.base.BaseTest;
import org.api.automation.bdd.context.ScenarioContext;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.response.AddPlaceResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cucumber lifecycle hooks: preconditions and cleanup.
 *
 * <p>PicoContainer injects the same {@link ScenarioContext} that the step definitions
 * receive, so state set here is visible to every step in the scenario.
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before(order = 0)
    public void logScenarioStart(Scenario scenario) {
        log.info("=== SCENARIO START: {} ===", scenario.getName());
    }

    /**
     * Creates a place for scenarios tagged {@code @DeletePlace}.
     *
     * <p>Running unconditionally means every execution of the scenario gets its own fresh
     * place. The old null-check meant the second run reused the first run's id.
     */
    @Before(value = "@DeletePlace", order = 10)
    public void createPlaceToDelete() {
        AddPlaceResponse created = RestAssured.given()
                .baseUri(ConfigManager.get(ConfigKeys.PLACES_BASE_URL))
                .queryParam("key", ConfigManager.get(ConfigKeys.PLACES_API_KEY))
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter(BaseTest.trafficLog()))
                .filter(new ResponseLoggingFilter(BaseTest.trafficLog()))
                .body(TestDataFactory.addPlace("Place To Delete", "English", "1 Cleanup Street"))
                .post(Paths.ADD_PLACE)
                .then().statusCode(200).extract().as(AddPlaceResponse.class);

        context.put("placeId", created.getPlaceId());
        log.info("Precondition: created place {} for the delete scenario", created.getPlaceId());
    }

    @After(order = 0)
    public void logScenarioResult(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("=== SCENARIO FAILED: {} — see target/logs/api-traffic.log ===",
                    scenario.getName());
        } else {
            log.info("=== SCENARIO {}: {} ===", scenario.getStatus(), scenario.getName());
        }
    }
}
