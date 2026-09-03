package org.api.automation.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for the Library Delete Book endpoint, which expects an upper-case {@code ID}. */
public class DeleteBookRequest {

    @JsonProperty("ID")
    private String id;

    public DeleteBookRequest() {
        // required by Jackson
    }

    public DeleteBookRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
