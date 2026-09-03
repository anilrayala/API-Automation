package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response body from e-commerce login. The {@code token} authorises every later call. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    private String token;
    private String userId;
    private String message;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** Never prints the token — it is a live credential for the session. */
    @Override
    public String toString() {
        return "LoginResponse{userId=" + userId + ", message=" + message + ", token=****}";
    }
}
