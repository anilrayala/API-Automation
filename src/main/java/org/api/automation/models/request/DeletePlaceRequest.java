package org.api.automation.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for Delete Place.
 *
 * <p>Worth a POJO even for a single field. The old code built this by concatenating the id
 * into a JSON string literal, which breaks silently the moment the id contains a quote or
 * a backslash. Jackson escapes the value correctly.
 */
public class DeletePlaceRequest {

    @JsonProperty("place_id")
    private String placeId;

    public DeletePlaceRequest() {
        // required by Jackson
    }

    public DeletePlaceRequest(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
