package org.api.automation.models.request;

import java.util.List;

/**
 * Request body for Create Order.
 *
 * <p>Serializes to {@code { "orders": [ { "country": ..., "productOrderedId": ... } ] }} —
 * a list of nested objects, which is exactly the shape hand-written JSON strings handle
 * worst.
 */
public class CreateOrderRequest {

    private List<OrderDetail> orders;

    public CreateOrderRequest() {
        // required by Jackson
    }

    public CreateOrderRequest(List<OrderDetail> orders) {
        this.orders = orders;
    }

    /** Convenience for the common single-item order. */
    public static CreateOrderRequest forSingleProduct(String country, String productId) {
        return new CreateOrderRequest(List.of(new OrderDetail(country, productId)));
    }

    public List<OrderDetail> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDetail> orders) {
        this.orders = orders;
    }
}
