package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/** The {@code courses} object: three arrays of {@link Course}, keyed by discipline. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseCatalog {

    private List<Course> webAutomation;
    private List<Course> api;
    private List<Course> mobile;

    public List<Course> getWebAutomation() {
        return webAutomation;
    }

    public void setWebAutomation(List<Course> webAutomation) {
        this.webAutomation = webAutomation;
    }

    public List<Course> getApi() {
        return api;
    }

    public void setApi(List<Course> api) {
        this.api = api;
    }

    public List<Course> getMobile() {
        return mobile;
    }

    public void setMobile(List<Course> mobile) {
        this.mobile = mobile;
    }

    /** Every course across all three disciplines, for catalog-wide assertions. */
    public List<Course> all() {
        List<Course> combined = new ArrayList<>();
        if (webAutomation != null) {
            combined.addAll(webAutomation);
        }
        if (api != null) {
            combined.addAll(api);
        }
        if (mobile != null) {
            combined.addAll(mobile);
        }
        return combined;
    }
}
