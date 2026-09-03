package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single course entry.
 *
 * <p>The original framework had three classes for this — {@code WebAutomation},
 * {@code API} and {@code Mobile} — each with identical {@code courseTitle} and
 * {@code price} fields, because the JSON nests the arrays under three different keys.
 *
 * <p>That is not required. Jackson binds by <i>shape</i>, not by name: the key that holds
 * an array only determines the field name in {@link CourseCatalog}, not the element type.
 * One class serves all three arrays, and now a change to the course shape is a change in
 * one file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    private String courseTitle;

    /**
     * Deliberately a String, not an int.
     *
     * <p>This API returns prices as quoted strings ({@code "price": "50"}). Modelling it as
     * {@code int} makes Jackson coerce, which works until a price arrives as {@code "50.00"}
     * or {@code ""}. Match the wire format and convert where you need arithmetic.
     */
    private String price;

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    /** The price as a number, for sum and comparison assertions. */
    public int priceAsInt() {
        return Integer.parseInt(price.trim());
    }

    @Override
    public String toString() {
        return courseTitle + " (" + price + ")";
    }
}
