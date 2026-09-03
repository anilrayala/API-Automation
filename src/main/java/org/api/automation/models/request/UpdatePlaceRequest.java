package org.api.automation.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for Update Place. Only the address is mutable in this API. */
public class UpdatePlaceRequest {

    @JsonProperty("place_id")
    private String placeId;

    private String address;
    private String key;

    public UpdatePlaceRequest() {
        // required by Jackson
    }

    public UpdatePlaceRequest(String placeId, String address, String key) {
        this.placeId = placeId;
        this.address = address;
        this.key = key;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
