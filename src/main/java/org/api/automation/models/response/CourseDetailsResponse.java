package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Optional;

/**
 * The OAuth-protected Get Course Details response — the nested-deserialization example.
 *
 * <p>Three levels deep: this object holds a {@link CourseCatalog}, which holds lists of
 * {@link Course}. REST Assured builds the whole tree with a single
 * {@code .as(CourseDetailsResponse.class)}, which is the payoff over walking the same
 * structure with {@code getString("courses.api[1].price")} strings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseDetailsResponse {

    private String instructor;
    private String url;
    private String services;
    private String expertise;
    private String linkedIn;
    private CourseCatalog courses;

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    public CourseCatalog getCourses() {
        return courses;
    }

    public void setCourses(CourseCatalog courses) {
        this.courses = courses;
    }

    /**
     * Finds a course by title anywhere in the catalog.
     *
     * <p>Returns {@link Optional} rather than {@code null}, so a test that looks for a
     * course that is not there fails on an explicit "not found" assertion instead of an
     * unexplained {@code NullPointerException}. This is the typed equivalent of the
     * conditional JsonPath lookups — iterate and match on a predicate.
     */
    public Optional<Course> findCourseByTitle(String title) {
        if (courses == null) {
            return Optional.empty();
        }
        return courses.all().stream()
                .filter(course -> title.equalsIgnoreCase(course.getCourseTitle()))
                .findFirst();
    }

    /** Titles of every web automation course, in response order. */
    public List<String> webAutomationTitles() {
        return courses.getWebAutomation().stream()
                .map(Course::getCourseTitle)
                .toList();
    }
}
