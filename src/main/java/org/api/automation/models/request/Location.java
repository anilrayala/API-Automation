package org.api.automation.models.request;

/**
 * Nested object inside {@link AddPlaceRequest}.
 *
 * <p>A nested POJO is how you model nested JSON: Jackson serializes this instance into the
 * {@code "location": { "lat": ..., "lng": ... }} block automatically. That is the whole
 * advantage over hand-built JSON strings — the structure is expressed once, in types.
 */
public class Location {

    private double lat;
    private double lng;

    public Location() {
        // required by Jackson for deserialization
    }

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
