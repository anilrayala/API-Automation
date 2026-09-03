package org.api.automation.models.request;

/**
 * Request body for the Library Add Book endpoint.
 *
 * <p>{@code isbn} and {@code aisle} are the fields the data-driven tests vary. The API
 * concatenates them into the book id it returns, so each data set must be unique or the
 * API replies that the book already exists.
 */
public class AddBookRequest {

    private String name;
    private String isbn;
    private String aisle;
    private String author;

    public AddBookRequest() {
        // required by Jackson
    }

    public AddBookRequest(String name, String isbn, String aisle, String author) {
        this.name = name;
        this.isbn = isbn;
        this.aisle = aisle;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /** The id the Library API derives from isbn + aisle, useful for cleanup assertions. */
    public String expectedBookId() {
        return isbn + aisle;
    }
}
