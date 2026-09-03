package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response body from Add Product. {@code productId} is needed to place and clean up an order. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddProductResponse {

    private String message;
    private String productId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
