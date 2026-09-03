package org.api.automation.models.request;

/** Credentials body for the e-commerce login endpoint. */
public class LoginRequest {

    private String userEmail;
    private String userPassword;

    public LoginRequest() {
        // required by Jackson
    }

    public LoginRequest(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Masks the password deliberately.
     *
     * <p>Without this, a failed assertion or a debug log that prints the request object
     * leaks the credential into console output and into the Jenkins build log.
     */
    @Override
    public String toString() {
        return "LoginRequest{userEmail=" + userEmail + ", userPassword=****}";
    }
}
