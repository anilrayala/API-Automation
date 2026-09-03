package org.api.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Response body from Create Order.
 *
 * <p>Both id fields are arrays because the endpoint accepts a batch of orders. The old code
 * reached into them with {@code js.getString("orders[0]")} — index arithmetic inside a
 * string. Typed as {@code List<String>}, the same access is {@code getOrders().get(0)} and
 * you can assert on the list size, which the string form cannot express.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderResponse {

    private String message;
    private List<String> orders;
    private List<String> productOrderId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    public List<String> getProductOrderId() {
        return productOrderId;
    }

    public void setProductOrderId(List<String> productOrderId) {
        this.productOrderId = productOrderId;
    }

    /** The id of the first (usually only) order created. */
    public String firstOrderId() {
        return orders == null || orders.isEmpty() ? null : orders.get(0);
    }
}
