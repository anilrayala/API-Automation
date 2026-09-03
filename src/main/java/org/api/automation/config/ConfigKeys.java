package org.api.automation.config;

/**
 * Names of the properties the framework reads.
 *
 * <p>Kept as constants so a typo is a compile error instead of a silent {@code null} at
 * runtime, and so you can find every usage of a setting with one "find usages".
 */
public final class ConfigKeys {

    // --- Base URLs (one per system under test) ---
    public static final String PLACES_BASE_URL = "places.baseUrl";
    public static final String LIBRARY_BASE_URL = "library.baseUrl";
    public static final String ECOM_BASE_URL = "ecom.baseUrl";
    public static final String OAUTH_BASE_URL = "oauth.baseUrl";
    public static final String GRAPHQL_BASE_URL = "graphql.baseUrl";
    public static final String GOOGLE_TOKEN_URL = "google.token.url";

    // --- Places API ---
    public static final String PLACES_API_KEY = "places.apiKey";

    // --- E-commerce API credentials ---
    public static final String ECOM_USER_EMAIL = "ecom.userEmail";
    public static final String ECOM_USER_PASSWORD = "ecom.userPassword";

    // --- OAuth 2.0 ---
    public static final String OAUTH_CLIENT_ID = "oauth.clientId";
    public static final String OAUTH_CLIENT_SECRET = "oauth.clientSecret";
    public static final String OAUTH_SCOPE = "oauth.scope";
    public static final String OAUTH_REDIRECT_URI = "oauth.redirectUri";

    // --- Framework behaviour ---
    public static final String LOG_REQUESTS = "framework.logRequests";
    public static final String RETRY_COUNT = "framework.retryCount";
    public static final String RELAXED_HTTPS = "framework.relaxedHttps";

    private ConfigKeys() {
        // constants only
    }
}
