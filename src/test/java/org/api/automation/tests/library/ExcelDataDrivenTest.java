package org.api.automation.tests.library;

import io.restassured.response.Response;
import org.api.automation.base.BaseTest;
import org.api.automation.constants.Paths;
import org.api.automation.data.ExcelDataReader;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.DeleteBookRequest;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Spreadsheet-driven test data: Excel row -> Map -> JSON request body.
 *
 * <p>Reading by column name rather than by position means an inserted spreadsheet column
 * cannot silently shift the data a test reads.
 *
 * <p>Tagged {@code external-api}: the Library endpoint is often offline.
 */
public class ExcelDataDrivenTest extends BaseTest {

    private static final String WORKBOOK   = "testdata/TestData.xlsx";
    private static final String SHEET      = "TestData";
    private static final String KEY_COLUMN = "TestCases";

    @Test(groups = {"regression", "datadriven", "external-api"})
    public void shouldAddBookUsingSpreadsheetData() {
        Map<String, String> row = ExcelDataReader.getRow(WORKBOOK, SHEET, KEY_COLUMN, "AddBook");
        log.info("Spreadsheet row: {}", row);

        assertEquals(row.get("Data4"), "227",
                "Numeric cells must read as '227', not '227.0' — see NumberToTextConverter");

        String isbn = row.get("Data3") + TestDataFactory.uniqueSuffix();
        String aisle = row.get("Data4");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", row.get("Data1"));
        body.put("author", row.get("Data2"));
        body.put("isbn", isbn);
        body.put("aisle", aisle);

        Response response = given(librarySpec())
                .body(body)
                .post(Paths.ADD_BOOK)
                .then().statusCode(200).extract().response();

        String bookId = response.jsonPath().getString("ID");
        assertNotNull(bookId, "Book ID");
        assertEquals(bookId, isbn + aisle, "Id should be isbn + aisle from the spreadsheet");

        given(librarySpec()).body(new DeleteBookRequest(bookId)).post(Paths.DELETE_BOOK);
    }

    /** Runs offline — only reads the file. Confirms header-keyed lookup works. */
    @Test(groups = {"offline", "regression"})
    public void shouldReadRowsByKeyNotByPosition() {
        Map<String, String> addBook = ExcelDataReader.getRow(WORKBOOK, SHEET, KEY_COLUMN, "AddBook");
        Map<String, String> login   = ExcelDataReader.getRow(WORKBOOK, SHEET, KEY_COLUMN, "Login");

        assertEquals(addBook.get("TestCases"), "AddBook");
        assertEquals(addBook.get("Data1"), "Appium");
        assertEquals(addBook.get("Data2"), "John Doe");

        assertEquals(login.get("TestCases"), "Login");
        assertEquals(login.get("Data1"), "abc",
                "Different key must return a different row");
    }

    /** A missing row must fail with a readable message. */
    @Test(groups = {"offline"}, expectedExceptions = IllegalArgumentException.class)
    public void shouldFailClearlyWhenTestCaseRowIsMissing() {
        ExcelDataReader.getRow(WORKBOOK, SHEET, KEY_COLUMN, "NoSuchTestCase");
    }
}
