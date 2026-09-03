package org.api.automation.data;

import org.api.automation.models.request.AddBookRequest;
import org.api.automation.models.request.AddPlaceRequest;
import org.api.automation.models.request.Location;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds request payloads for tests.
 *
 * <p>Separating "what the payload looks like" from "what this test is checking" means a
 * test only states the fields it actually cares about. Everything else gets a sensible
 * default from here, so adding a new required field to an API is one edit in this class
 * rather than an edit in every test.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // static factory
    }

    /**
     * An Add Place payload with the three fields tests usually vary, and defaults for the rest.
     */
    public static AddPlaceRequest addPlace(String name, String language, String address) {
        AddPlaceRequest request = new AddPlaceRequest();
        request.setName(name);
        request.setLanguage(language);
        request.setAddress(address);
        request.setAccuracy(50);
        request.setPhoneNumber("(+91) 983 893 3937");
        request.setWebsite("https://rahulshettyacademy.com");
        request.setTypes(List.of("shoe park", "shop"));
        request.setLocation(new Location(-38.383494, 33.427362));
        return request;
    }

    /** An Add Place payload with every field defaulted, for tests that do not care about content. */
    public static AddPlaceRequest defaultPlace() {
        return addPlace("Frontline House", "English", "29, side layout, cohen 09");
    }

    /**
     * An Add Book payload whose isbn is unique per call.
     *
     * <p>This matters more than it looks. The Library API derives the book id from
     * isbn + aisle and rejects a duplicate, so a test using a fixed isbn passes the first
     * time it ever runs and fails on every run afterwards. Tests that are not repeatable are
     * not tests. The suffix combines a timestamp with a random component so parallel threads
     * cannot collide either.
     */
    public static AddBookRequest uniqueBook(String aisle) {
        return new AddBookRequest(
                "Learn Appium Automation with Java",
                "isbn" + uniqueSuffix(),
                aisle,
                "John Foer");
    }

    /** A book with a caller-supplied isbn, for tests that assert on the exact id. */
    public static AddBookRequest book(String isbn, String aisle) {
        return new AddBookRequest("Learn Appium Automation with Java", isbn, aisle, "John Foer");
    }

    /** A value unique across runs and across parallel threads. */
    public static String uniqueSuffix() {
        return System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
