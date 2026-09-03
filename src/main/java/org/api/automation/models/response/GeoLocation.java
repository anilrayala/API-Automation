package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@code location} object as Get Place <b>returns</b> it.
 *
 * <h2>Why this is not the same class as the request's Location</h2>
 * The Places API is asymmetric. Add Place accepts:
 * <pre>
 *   "location": { "lat": -38.383494, "lng": 33.427362 }
 * </pre>
 * and Get Place returns:
 * <pre>
 *   "location": { "latitude": -38.383494, "longitude": 33.427362 }
 * </pre>
 *
 * <p>Different field names for the same concept, in the same API. Reusing one POJO for both
 * directions therefore fails — and it failed loudly, with
 * {@code UnrecognizedPropertyException: Unrecognized field "latitude"}, which is exactly the
 * behaviour you want from a strict request model.
 *
 * <p>This is the general argument for separate request and response models. They look like
 * duplication right up until the API does something like this, and then the split is what
 * lets each side describe the wire format honestly instead of one of them lying.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
