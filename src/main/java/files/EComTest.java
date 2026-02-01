package files;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import pojo.OrderDetails;
import pojo.Orders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class EComTest {
    public static void main(String[] args) {

        // Login - generate token
        RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType("application/json").build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserEmail("anil45@gmail.com");
        loginRequest.setUserPassword("Anil@1234");

        RequestSpecification reqLogin = given().relaxedHTTPSValidation().log().all().spec(req).body(loginRequest);

        LoginResponse loginResponse = reqLogin.when().log().all().post("/api/ecom/auth/login")
                .then().log().all().extract().response().as(LoginResponse.class);

        String token = loginResponse.getToken();
        String userId = loginResponse.getUserId();
        System.out.println(loginResponse.getToken());
        System.out.println(loginResponse.getUserId());
        System.out.println(loginResponse.getMessage());

        // Add or Create product
        RequestSpecification reqAddProduct = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType("multipart/form-data")
                .addHeader("authorization", token).build();

        RequestSpecification reqAddProductBody = given().log().all().spec(reqAddProduct)
                .param("productName", "Laptop")
                .param("productAddedBy", userId)
                .param("productCategory", "Electronics")
                .param("productSubCategory", "Computer")
                .param("productPrice", "65000")
                .param("productDescription", "Dell Inspiron")
                .param("productFor", "All")
                .multiPart("productImage", new File("src/main/resources/laptop.jpg"));

        String addProductResponse = reqAddProductBody.when().log().all().post("/api/ecom/product/add-product")
                .then().log().all().extract().response().asString();

        JsonPath js = new JsonPath(addProductResponse);
        String productId = js.getString("productId");
        System.out.println(productId);

        // Create order
        RequestSpecification reqCreateOrder = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType("application/json")
                .addHeader("authorization", token).build();

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCountry("India");
        orderDetails.setProductOrderedId(productId);

        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);

        Orders orders = new Orders();
        orders.setOrders(orderDetailsList);

        RequestSpecification reqCreateOrderBody = given().log().all().spec(reqCreateOrder).body(orders);

        String createOrderResponse = reqCreateOrderBody.when().log().all().post("/api/ecom/order/create-order")
                .then().log().all().extract().response().asString();

        System.out.println(createOrderResponse);

        JsonPath js1 = new JsonPath(createOrderResponse);
        String ordersId = js1.getString("orders[0]");
        String ordersMessage = js1.getString("message");
        Assert.assertEquals(ordersMessage, "Order Placed Successfully");
        String productOrderId = js1.getString("productOrderId[0]");
        Assert.assertEquals(productOrderId, productId);

        System.out.println(ordersId);
        System.out.println(ordersMessage);
        System.out.println(productOrderId);


        // Delete product
        RequestSpecification reqDeleteProduct = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType("application/json")
                .addHeader("authorization", token).build();

        RequestSpecification reqDeleteProductBody = given().log().all().spec(reqDeleteProduct)
                .pathParam("productId", productId);

        /*
        Path param is used in delete request because productId is part of the endpoint URL
        .delete("/api/ecom/product/delete-product/{productId}")
         */
        String deleteProductResponse = reqDeleteProductBody.when().log().all()
                .delete("/api/ecom/product/delete-product/{productId}")
                .then().log().all().extract().response().asString();

        JsonPath js2 = new JsonPath(deleteProductResponse);
        String message = js2.getString("message");
        Assert.assertEquals(message, "Product Deleted Successfully");

        //Delete Order - Optional

        RequestSpecification reqDeleteOrder = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType("application/json")
                .addHeader("authorization", token).build();
        RequestSpecification reqDeleteOrderBody = given().log().all().spec(reqDeleteOrder)
                .pathParam("orderId", ordersId);
        String deleteOrderResponse = reqDeleteOrderBody.when().log().all()
                .delete("/api/ecom/order/delete-order/{orderId}")
                .then().log().all().extract().response().asString();

        JsonPath js3 = new JsonPath(deleteOrderResponse);
        String deleteMessage = js3.getString("message");
        Assert.assertEquals(deleteMessage, "Orders Deleted Successfully");
        System.out.println(deleteMessage);

//        // Get Order Details - Optional
//        RequestSpecification reqViewOrders = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
//                .setContentType("application/json")
//                .addHeader("authorization", token).build();
//        RequestSpecification reqViewOrdersBody = given().log().all().spec(reqViewOrders).queryParam("id", ordersId);
//        String viewOrdersResponse = reqViewOrdersBody.when().log().all()
//                .get("/api/ecom/order/get-orders-details")
//                .then().log().all().extract().response().asString();
//
//        JsonPath js4 = new JsonPath(viewOrdersResponse);
//        String viewMessage = js4.getString("message");
//        Assert.assertEquals(viewMessage, "Orders fetched Successfully");
//        System.out.println(viewMessage);

    }
}
