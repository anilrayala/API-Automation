package org.api.automation.constants;

/**
 * Every endpoint path the tests talk to.
 *
 * <p>Keeping paths here means a URL change is a one-line edit, and the constant name
 * (used in feature files) documents the intent.
 */
public final class Paths {

    private Paths() {}

    // --- Google Places ---
    public static final String ADD_PLACE    = "/maps/api/place/add/json";
    public static final String GET_PLACE    = "/maps/api/place/get/json";
    public static final String UPDATE_PLACE = "/maps/api/place/update/json";
    public static final String DELETE_PLACE = "/maps/api/place/delete/json";

    // --- Library API ---
    public static final String ADD_BOOK    = "/Library/Addbook.php";
    public static final String DELETE_BOOK = "/Library/DeleteBook.php";

    // --- E-commerce API ---
    public static final String ECOM_LOGIN          = "/api/ecom/auth/login";
    public static final String ECOM_ADD_PRODUCT    = "/api/ecom/product/add-product";
    public static final String ECOM_CREATE_ORDER   = "/api/ecom/order/create-order";
    public static final String ECOM_DELETE_PRODUCT = "/api/ecom/product/delete-product/{productId}";
    public static final String ECOM_DELETE_ORDER   = "/api/ecom/order/delete-order/{orderId}";

    // --- OAuth 2.0 ---
    public static final String OAUTH_TOKEN          = "/oauthapi/oauth2/resourceOwner/token";
    public static final String OAUTH_COURSE_DETAILS = "/oauthapi/getCourseDetails";

    // --- GraphQL ---
    public static final String GRAPHQL = "/gq/graphql";

    /**
     * Resolves a name written in a Cucumber feature file to the matching path constant.
     *
     * <p>The When step passes endpoint names as plain strings; this converts them. A typo
     * gets a readable error listing the valid names instead of a bare enum exception.
     */
    public static String fromName(String name) {
        return switch (name.toUpperCase()) {
            case "ADD_PLACE"    -> ADD_PLACE;
            case "GET_PLACE"    -> GET_PLACE;
            case "UPDATE_PLACE" -> UPDATE_PLACE;
            case "DELETE_PLACE" -> DELETE_PLACE;
            case "ADD_BOOK"     -> ADD_BOOK;
            case "DELETE_BOOK"  -> DELETE_BOOK;
            case "ECOM_LOGIN"   -> ECOM_LOGIN;
            case "GRAPHQL"      -> GRAPHQL;
            default -> throw new IllegalArgumentException(
                    "Unknown endpoint '" + name + "'. Valid: ADD_PLACE, GET_PLACE, "
                            + "UPDATE_PLACE, DELETE_PLACE, ADD_BOOK, DELETE_BOOK, ECOM_LOGIN, GRAPHQL");
        };
    }
}
