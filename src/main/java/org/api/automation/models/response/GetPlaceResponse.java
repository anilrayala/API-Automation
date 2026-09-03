package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Response body from Get Place — the stored place, echoed back.
 *
 * <p>The {@code location} field is a {@link GeoLocation}, not the request's
 * {@code Location}: this API returns {@code latitude}/{@code longitude} where it accepted
 * {@code lat}/{@code lng}. See {@link GeoLocation} for why that matters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPlaceResponse {

    private GeoLocation location;
    private int accuracy;
    private String name;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    /**
     * A comma-delimited String, not a list — another asymmetry in this API.
     *
     * <p>Add Place accepts {@code "types": ["shoe park", "shop"]} but Get Place returns
     * {@code "types": "shoe park,shop"}. Declaring this as {@code List<String>} produced
     * {@code MismatchedInputException: Cannot construct instance of java.util.ArrayList ...
     * from String value ('shoe park,shop')}.
     *
     * <p>The right response to that is to model what the API actually sends and expose a
     * convenience accessor ({@link #typesList()}), not to coerce Jackson into pretending the
     * shape is something else. A model that lies about the wire format moves the surprise
     * somewhere harder to find.
     */
    private String types;

    private String website;
    private String language;

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
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

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    /**
     * The {@code types} value split into a list, for assertions that want to compare against
     * what was submitted.
     */
    public List<String> typesList() {
        if (types == null || types.isBlank()) {
            return List.of();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .toList();
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
