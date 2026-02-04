package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/*
JUnit test runner class to execute Cucumber feature files.
- Specifies the location of feature files and step definitions.
- Configures reporting options for test results.
- using junit 4 annotations.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "stepDefinitions",
//    tags = "@DeletePlace",
    plugin = {
        "json:target/json-reports/cucumber-report.json",
        "pretty",
        "html:target/cucumber-reports.html"
    },
    monochrome = true
)
public class TestRunner {

}
