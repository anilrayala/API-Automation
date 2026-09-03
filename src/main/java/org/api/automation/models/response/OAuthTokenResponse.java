package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The standard OAuth 2.0 token response, defined by RFC 6749 section 5.1.
 *
 * <p>Because it is a standard, this one model works for every grant type — client
 * credentials, authorization code, password, refresh token — and for every provider. The
 * field names are fixed by the spec, which is why they are snake_case and need
 * {@code @JsonProperty} mappings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    /** Lifetime in seconds. Used to decide whether a cached token is still usable. */
    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /** Never prints the tokens. */
    @Override
    public String toString() {
        return "OAuthTokenResponse{tokenType=" + tokenType + ", expiresIn=" + expiresIn
                + ", scope=" + scope + ", accessToken=****}";
    }
}
