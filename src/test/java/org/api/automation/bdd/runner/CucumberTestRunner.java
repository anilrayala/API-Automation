package org.api.automation.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Runs the Cucumber feature files.
 *
 * <h2>Why this is a TestNG runner, not a JUnit one</h2>
 * The old runner was {@code @RunWith(Cucumber.class)} on JUnit 4, while the plan for the
 * rest of the suite is TestNG. Maven Surefire picks a single test provider per module, so
 * having JUnit 4 and TestNG side by side means one of the two silently does not run — a
 * genuinely nasty failure mode, because the build stays green while half the tests are
 * skipped.
 *
 * <p>{@code cucumber-testng} solves it: extending {@link AbstractTestNGCucumberTests}
 * exposes every scenario as a TestNG test. One provider, one {@code testng.xml}, one report,
 * and every Cucumber feature — feature files, hooks, tags, scenario outlines, the JSON
 * report Jenkins consumes — works exactly as before.
 *
 * <h2>Tag filtering</h2>
 * {@code tags} is not hardcoded here. It reads the {@code cucumber.filter.tags} property, so
 * the same runner serves every filter:
 * <pre>
 *   mvn test -Dcucumber.filter.tags="@AddPlace"
 *   mvn test -Dcucumber.filter.tags="@Regression and not @DeletePlace"
 * </pre>
 * The old runner had the filter commented out in source, which meant changing it required
 * an edit and a rebuild — and made it impossible for Jenkins to parameterise.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"org.api.automation.bdd.stepdefinitions"},
        plugin = {
                "pretty",
                // JSON feeds maven-cucumber-reporting, which builds the HTML Jenkins publishes
                "json:target/json-reports/cucumber-report.json",
                "html:target/cucumber-reports/cucumber.html",
                "summary"
        },
        monochrome = true
        // No 'strict' attribute: it was removed in Cucumber 6, because strict IS the
        // behaviour now. An undefined or pending step fails the scenario rather than
        // reporting it as skipped-and-passing, which is what you want — a feature step with
        // no matching definition is a defect in the suite.
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    /**
     * Scenario-level parallelism.
     *
     * <p>Overriding the inherited provider with {@code parallel = true} runs scenarios
     * concurrently. This is only safe because there is no static state left: each scenario
     * gets its own {@link org.api.automation.bdd.context.ScenarioContext} from
     * PicoContainer, and {@code SpecFactory} builds a fresh request spec per call. With the
     * old shared, mutable spec and {@code static placeId}, turning this on would have
     * produced cross-talk between scenarios.
     *
     * <p>Thread count comes from the {@code dataproviderthreadcount} setting in
     * {@code testng.xml}.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
