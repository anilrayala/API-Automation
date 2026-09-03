package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One model for the several endpoints whose entire response is an acknowledgement.
 *
 * <p>The practice APIs are frustratingly inconsistent about the field name — Update Place
 * returns {@code msg}, Delete Product returns {@code message}, Add Book returns {@code Msg}.
 * {@code @JsonAlias} accepts all of them into a single field, so one class covers
 * Update Place, Delete Place, Delete Book, Delete Product and Delete Order instead of five
 * near-identical POJOs.
 *
 * <p>This is worth knowing generally: {@code @JsonAlias} is for tolerating input variation
 * on deserialization, while {@code @JsonProperty} controls the name used when serializing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponse {

    @JsonProperty("message")
    @JsonAlias({"msg", "Msg", "MSG"})
    private String message;

    /** Present on the Places API responses; {@code null} elsewhere. */
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MessageResponse{message=" + message + ", status=" + status + "}";
    }
}
