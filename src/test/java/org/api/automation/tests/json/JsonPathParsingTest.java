package org.api.automation.tests.json;

import io.restassured.path.json.JsonPath;
import org.api.automation.base.BaseTest;
import org.api.automation.utils.JsonUtils;
import org.api.automation.utils.ResourceReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;

/**
 * JsonPath parsing of a complex nested payload.
 *
 * <p>This is the old {@code ComplexJsonParse} class, which was a {@code main()} method
 * printing to the console — so nothing ever verified any of it. Every {@code System.out}
 * is now an assertion, and the payload has moved out of a Java string constant into
 * {@code testdata/course-price.json}.
 *
 * <p>The suite runs with no network access, which makes it the right place to start when
 * something is broken: if these fail, the problem is the framework, not the environment.
 */
public class JsonPathParsingTest extends BaseTest {

    private static final String PAYLOAD_PATH = "testdata/course-price.json";

    private JsonPath json;

    /**
     * Parses the payload once for the whole class.
     *
     * <p>{@code alwaysRun = true} is required, not optional. When a suite filters by group
     * — as {@code offline.xml} and {@code smoke.xml} do — TestNG only runs configuration
     * methods that belong to an included group. A {@code @BeforeClass} with no {@code groups}
     * of its own is silently skipped, and every test in the class then fails with a
     * {@link NullPointerException} on the field it was supposed to populate.
     *
     * <p>This is worth remembering as a rule: setup and teardown methods should carry
     * {@code alwaysRun = true} unless you have a specific reason to gate them by group.
     */
    @BeforeClass(alwaysRun = true)
    public void parsePayload() {
        json = JsonUtils.toJsonPath(ResourceReader.readAsString(PAYLOAD_PATH));
    }

    /**
     * Array size.
     *
     * <p>{@code courses.size()} is Groovy's GPath, not standard JSONPath. REST Assured
     * evaluates the expression as Groovy, so collection methods like {@code size()},
     * {@code findAll} and {@code collect} are available inside the path string.
     */
    @Test(groups = {"offline", "smoke"})
    public void shouldReturnExpectedNumberOfCourses() {
        assertEquals(json.getInt("courses.size()"), 4, "Course count");
    }

    /** Reaching into a nested object with dot notation. */
    @Test(groups = {"offline", "smoke"})
    public void shouldReadNestedDashboardValue() {
        assertEquals(json.getInt("dashboard.purchaseAmount"), 1162, "Purchase amount");
        assertEquals(json.getString("dashboard.website"), "rahulshettyacademy.com", "Website");
    }

    /** Indexing into an array. Zero-based, like Java. */
    @Test(groups = {"offline"})
    public void shouldReadFirstCourseByIndex() {
        assertEquals(json.getString("courses[0].title"), "Selenium Python", "First course title");
        // Negative indexes count from the end — a Groovy convenience worth knowing
        assertEquals(json.getString("courses[-1].title"), "Appium", "Last course title");
    }

    /**
     * Collecting one field from every element.
     *
     * <p>{@code "courses.title"} on an array returns the list of all titles. The old code
     * built this with a {@code for} loop and string-concatenated indexes; the path does it
     * in one expression.
     */
    @Test(groups = {"offline"})
    public void shouldCollectAllCourseTitles() {
        List<String> titles = json.getList("courses.title");

        // contains() asserts exact contents AND order
        assertThat("All course titles", titles,
                contains("Selenium Python", "Cypress", "RPA", "Appium"));
        // hasItem() asserts presence only — use it when order is not part of the contract
        assertThat(titles, hasItem("RPA"));
    }

    /**
     * Conditional lookup — find the element matching a predicate.
     *
     * <p>{@code find} returns the first match, {@code findAll} returns every match. This
     * replaces the loop-and-break the old code used to locate the RPA course, and it fails
     * cleanly (null) rather than silently doing nothing when there is no match.
     */
    @Test(groups = {"offline"})
    public void shouldFindCopiesSoldForSpecificCourse() {
        int copies = json.getInt("courses.find { it.title == 'RPA' }.copies");
        assertEquals(copies, 10, "Copies sold for RPA");
    }

    /** findAll with a numeric predicate, then a collection operation on the result. */
    @Test(groups = {"offline"})
    public void shouldFindAllCoursesAbovePriceThreshold() {
        List<String> expensive = json.getList("courses.findAll { it.price > 40 }.title");
        assertThat("Courses priced above 40", expensive, contains("Selenium Python", "RPA"));
    }

    /**
     * The business-logic check: does the dashboard total actually reconcile with the line
     * items?
     *
     * <p>This is the assertion that matters most in the whole class — it is the only one
     * that could catch a real defect in the API rather than confirming a value is present.
     * The old code computed the sum and printed whether it matched, so a mismatch would
     * have scrolled past in the console with the build still green.
     */
    @Test(groups = {"offline", "smoke"})
    public void purchaseAmountShouldEqualSumOfCourseLineItems() {
        int expectedTotal = json.getInt("dashboard.purchaseAmount");

        int calculatedTotal = 0;
        List<Integer> prices = json.getList("courses.price");
        List<Integer> copies = json.getList("courses.copies");
        for (int i = 0; i < prices.size(); i++) {
            calculatedTotal += prices.get(i) * copies.get(i);
        }

        assertThat(
                "Dashboard purchaseAmount should equal the sum of price x copies over all courses",
                calculatedTotal, equalTo(expectedTotal));
    }
}
