package files;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;



public class DynamicJson {

    /*
    Test method to add a book using dynamic data provided by the DataProvider
    it takes two parameters, isbn and aisle, which are supplied by the DataProvider named "BooksData"
    the method constructs the request body using the Payload.AddBook method, sends a POST request to add the book,
    and extracts the book ID from the response
     */
    @Test(dataProvider = "BooksData")
    public void addBook(String isbn, String aisle){

        RestAssured.baseURI = "http://216.10.245.166";
        String response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .body(Payload.AddBook(isbn, aisle))
        .when()
                .post("/Library/Addbook.php")
        .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = ReusableMethods.rawToJson(response);
        String bookID = jsonPath.getString("ID");
        System.out.println("Book ID is: " + bookID);

    }

    /*
    DataProvider method to supply multiple sets of data to the test method
    it returns a 2D Object array where each sub-array represents a set of parameters for the test method
    and is annotated with @DataProvider to be recognized by TestNG
     */
    @DataProvider(name="BooksData")
    public Object[][] getData(){
        // Array = Collection of elements
        // Object Array = Collection of different type of elements

        return new Object[][] {{"abc1","12345"},{"abc2","54321"},{"abc3","67890"}};
    }

    /*
    Test method to delete a book using dynamic book IDs provided by the DataProvider
    it takes one parameter, bookID, which is supplied by the DataProvider named "deleteBookData"
    the method constructs the request body using the Payload.DeleteBook method, sends a POST request to delete the book,
    and prints the response message
     */
    @Test(dataProvider = "deleteBookData")
    public void deleteBook(String bookID) {

        RestAssured.baseURI = "http://216.10.245.166";
        String response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .body(Payload.DeleteBook(bookID))
                .when()
                .delete("/Library/DeleteBook.php")
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = ReusableMethods.rawToJson(response);
        String msg = jsonPath.getString("msg");
        System.out.println("Response message: " + msg);
    }

    @DataProvider(name="deleteBookData")
    public Object[][] deleteBookData() {
        return new Object[][] {{"abc112345"},{"abc254321"},{"abc367890"}};
    }

}
