package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Response body from Library Add Book. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddBookResponse {

    @JsonProperty("Msg")
    @JsonAlias({"msg", "message"})
    private String message;

    @JsonProperty("ID")
    @JsonAlias({"id"})
    private String id;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
