package org.api.automation.tests.ecom;

import io.restassured.http.ContentType;
import org.api.automation.base.BaseTest;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.CreateOrderRequest;
import org.api.automation.models.request.LoginRequest;
import org.api.automation.models.response.AddProductResponse;
import org.api.automation.models.response.CreateOrderResponse;
import org.api.automation.models.response.LoginResponse;
import org.api.automation.models.response.MessageResponse;
import org.api.automation.utils.ResourceReader;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Full e-commerce flow: log in, upload a product, order it, clean up.
 *
 * <p>Login runs in {@code @BeforeClass} — it is a precondition, not the thing under test.
 * Cleanup runs in {@code @AfterClass(alwaysRun=true)} so it executes even when a test fails.
 *
 * <p>Tagged {@code requires-credentials}: set {@code ECOM_USEREMAIL} and
 * {@code ECOM_USERPASSWORD} as environment variables before running.
 */
public class EcomOrderE2ETest extends BaseTest {

    private String token;
    private String userId;
    private String productId;
    private String orderId;

    @BeforeClass(alwaysRun = true)
    public void login() {
        LoginRequest credentials = new LoginRequest(
                ConfigManager.getRequired(ConfigKeys.ECOM_USER_EMAIL),
                ConfigManager.getRequired(ConfigKeys.ECOM_USER_PASSWORD));

        LoginResponse session = given(ecomSpec())
                .body(credentials)
                .post(Paths.ECOM_LOGIN)
                .then().statusCode(200).extract().as(LoginResponse.class);

        assertThat("Auth token", session.getToken(), not(emptyOrNullString()));
        assertNotNull(session.getUserId(), "userId is needed for product upload");

        token = session.getToken();
        userId = session.getUserId();
        log.info("Authenticated as {}", session);
    }

    /**
     * Adds a product with a multipart image upload.
     *
     * <p>Multipart requests use {@code .multiPart()} not {@code .body()}: each form part
     * is a separate field, and the file is one of those parts. REST Assured sets the MIME
     * boundaries automatically.
     */
    @Test(groups = {"e2e", "regression", "requires-credentials"})
    public void shouldAddProductWithImageUpload() {
        AddProductResponse response = given(ecomSpec(token))
                .contentType(ContentType.MULTIPART)
                .param("productName", "Laptop " + TestDataFactory.uniqueSuffix())
                .param("productAddedBy", userId)
                .param("productCategory", "Electronics")
                .param("productSubCategory", "Computer")
                .param("productPrice", "65000")
                .param("productDescription", "Dell Inspiron")
                .param("productFor", "All")
                .multiPart("productImage", ResourceReader.asFile("testdata/laptop.jpg"))
                .post(Paths.ECOM_ADD_PRODUCT)
                .then().statusCode(200).extract().as(AddProductResponse.class);

        assertThat("productId", response.getProductId(), not(emptyOrNullString()));
        productId = response.getProductId();
        log.info("Created product {}", productId);
    }

    @Test(groups = {"e2e", "regression", "requires-credentials"},
            dependsOnMethods = "shouldAddProductWithImageUpload")
    public void shouldCreateOrderForProduct() {
        CreateOrderResponse response = given(ecomSpec(token))
                .body(CreateOrderRequest.forSingleProduct("India", productId))
                .post(Paths.ECOM_CREATE_ORDER)
                .then().extract().as(CreateOrderResponse.class);

        assertEquals(response.getMessage(), "Order Placed Successfully", "Order acknowledgement");
        assertNotNull(response.firstOrderId(), "Order id");
        assertThat("Order must reference the product we created",
                response.getProductOrderId(), contains(productId));

        orderId = response.firstOrderId();
        log.info("Created order {}", orderId);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (token == null) {
            log.warn("Login never completed — nothing to clean up");
            return;
        }
        if (orderId != null) {
            try {
                MessageResponse r = given(ecomSpec(token))
                        .pathParam("orderId", orderId)
                        .delete(Paths.ECOM_DELETE_ORDER)
                        .then().statusCode(200).extract().as(MessageResponse.class);
                log.info("Deleted order {}: {}", orderId, r.getMessage());
            } catch (RuntimeException e) {
                log.warn("Could not delete order {}: {}", orderId, e.getMessage());
            }
        }
        if (productId != null) {
            try {
                MessageResponse r = given(ecomSpec(token))
                        .pathParam("productId", productId)
                        .delete(Paths.ECOM_DELETE_PRODUCT)
                        .then().statusCode(200).extract().as(MessageResponse.class);
                log.info("Deleted product {}: {}", productId, r.getMessage());
            } catch (RuntimeException e) {
                log.warn("Could not delete product {}: {}", productId, e.getMessage());
            }
        }
    }
}
