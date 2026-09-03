package org.api.automation.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request body for Add Place — the serialization example.
 *
 * <p>REST Assured hands this object to Jackson when you pass it to {@code .body(...)} with
 * a JSON content type, so the outgoing payload is generated from the fields below. That
 * replaces the hand-concatenated JSON string the old {@code Payload.AddPlace()} returned,
 * where a missing comma or quote was a runtime failure rather than a compile error.
 *
 * <p>{@code @JsonProperty} is doing real work here: the API expects {@code phone_number} in
 * snake_case, but Java fields use camelCase. The annotation maps between them so neither
 * side has to compromise — the old POJO instead named the field {@code phone_number} and
 * exposed a {@code getPhone_number()} getter to work around it.
 */
public class AddPlaceRequest {

    private Location location;
    private int accuracy;
    private String name;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;
    private List<String> types;
    private String website;
    private String language;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
