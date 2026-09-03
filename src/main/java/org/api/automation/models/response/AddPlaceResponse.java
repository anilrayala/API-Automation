package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body from Add Place — the deserialization example.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} is on every response model in this
 * package, and it matters: without it, the day the API adds one new field, every test that
 * deserializes this breaks with {@code UnrecognizedPropertyException}. Requests should be
 * strict; responses should tolerate additions.
 *
 * <p>Typing the response is what turns {@code js.getString("place_id")} — a string that no
 * compiler checks — into {@code response.getPlaceId()}, which fails at build time if the
 * field is renamed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddPlaceResponse {

    private String status;

    @JsonProperty("place_id")
    private String placeId;

    private String scope;
    private String reference;
    private String id;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
