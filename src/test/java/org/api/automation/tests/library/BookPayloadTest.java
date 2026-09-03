package org.api.automation.tests.library;

import io.restassured.response.Response;
import org.api.automation.base.BaseTest;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.AddBookRequest;
import org.api.automation.models.request.DeleteBookRequest;
import org.api.automation.models.response.AddBookResponse;
import org.api.automation.utils.ResourceReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Four ways to supply a request body — POJO, JSON template, Map, and data-driven.
 *
 * <p>All four produce identical JSON on the wire. The choice is about maintainability:
 * <ol>
 *   <li>POJO — compile-checked and refactorable. The default choice.</li>
 *   <li>JSON file with placeholders — when a non-Java teammate needs to read the payload.</li>
 *   <li>Map — natural bridge for spreadsheet-driven data which is already key/value pairs.</li>
 * </ol>
 *
 * <p>Tagged {@code external-api}: the Library endpoint is often offline.
 */
public class BookPayloadTest extends BaseTest {

    /** 1. POJO serialized by Jackson. */
    @Test(groups = {"regression", "external-api"})
    public void shouldAddBookFromPojoPayload() {
        AddBookRequest request = TestDataFactory.uniqueBook("2645");

        AddBookResponse response = given(librarySpec())
                .body(request)
                .post(Paths.ADD_BOOK)
                .then().statusCode(200).extract().as(AddBookResponse.class);

        assertNotNull(response.getId(), "Book ID");
        assertEquals(response.getId(), request.expectedBookId(),
                "This API derives the id from isbn + aisle");

        given(librarySpec()).body(new DeleteBookRequest(response.getId())).post(Paths.DELETE_BOOK);
    }

    /**
     * 2. A JSON file with {{token}} substitution.
     *
     * <p>Reads through the classpath (not a relative file path) so it works under Jenkins.
     */
    @Test(groups = {"regression", "external-api"})
    public void shouldAddBookFromJsonFileTemplate() {
        String isbn = "tmpl" + TestDataFactory.uniqueSuffix();

        String body = ResourceReader.readJsonTemplate("payloads/add-book.json",
                "isbn", isbn, "aisle", "2645");

        Response response = given(librarySpec())
                .body(body)
                .post(Paths.ADD_BOOK)
                .then().statusCode(200).extract().response();

        String bookId = response.jsonPath().getString("ID");
        assertEquals(bookId, isbn + "2645", "Book ID from templated payload");

        given(librarySpec()).body(new DeleteBookRequest(bookId)).post(Paths.DELETE_BOOK);
    }

    /**
     * 3. A Map — REST Assured serializes it to JSON automatically.
     *
     * <p>A nested Map value becomes a nested JSON object. This is how spreadsheet rows work.
     */
    @Test(groups = {"regression", "external-api"})
    public void shouldAddBookFromMapPayload() {
        String isbn = "map" + TestDataFactory.uniqueSuffix();

        Map<String, Object> location = new LinkedHashMap<>();
        location.put("latitude", "-38.383494");
        location.put("longitude", "33.427362");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Learn Appium Automation with Java");
        body.put("isbn", isbn);
        body.put("aisle", "2645");
        body.put("author", "John Foer");
        body.put("location", location);

        Response response = given(librarySpec())
                .body(body)
                .post(Paths.ADD_BOOK)
                .then().statusCode(200).extract().response();

        String bookId = response.jsonPath().getString("ID");
        assertEquals(bookId, isbn + "2645", "Book ID from map payload");

        given(librarySpec()).body(new DeleteBookRequest(bookId)).post(Paths.DELETE_BOOK);
    }

    @DataProvider(name = "aisles")
    public Object[][] aisles() {
        return new Object[][]{{"12345"}, {"54321"}, {"67890"}};
    }

    /** Data-driven add-and-delete. ISBNs are generated so the test is repeatable. */
    @Test(dataProvider = "aisles", groups = {"regression", "datadriven", "external-api"})
    public void shouldAddThenDeleteBook(String aisle) {
        AddBookRequest request = TestDataFactory.uniqueBook(aisle);

        AddBookResponse added = given(librarySpec())
                .body(request)
                .post(Paths.ADD_BOOK)
                .then().statusCode(200).extract().as(AddBookResponse.class);

        assertNotNull(added.getId(), "Book ID for aisle " + aisle);

        given(librarySpec())
                .body(new DeleteBookRequest(added.getId()))
                .post(Paths.DELETE_BOOK)
                .then().statusCode(200);
    }
}
