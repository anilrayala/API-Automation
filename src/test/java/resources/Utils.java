package resources;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.*;
import java.util.Properties;

/*
    Utility class to provide shared RequestSpecification and ResponseSpecification
    to avoid redundant code and improve performance by reusing instances.
 */
public class Utils {

    private static volatile RequestSpecification reqSpec;
    private static volatile ResponseSpecification resSpec;
    private static final Object LOCK = new Object();

    /*
        Provides a singleton RequestSpecification instance with base URI,
        JSON content type, and logging filters. Uses double-checked locking
        for thread safety.
     */
    public static RequestSpecification requestSpecification() throws FileNotFoundException {
        if (reqSpec == null) {
            synchronized (LOCK) {
                if (reqSpec == null) {
                    PrintStream log = new PrintStream(new FileOutputStream("logging.txt", true), true);
                    RequestSpecBuilder rb = new RequestSpecBuilder();
                    rb.setBaseUri(getGlobalValue("baseUrl"));
                    rb.addQueryParam("key", "qaclick123");
                    rb.setContentType(ContentType.JSON);
                    // Add logging filters so requests and responses are logged automatically
                    rb.addFilter(RequestLoggingFilter.logRequestTo(log));
                    rb.addFilter(ResponseLoggingFilter.logResponseTo(log));
                    reqSpec = rb.build();
                    return reqSpec;
                }
            }
        }
        return reqSpec;
    }

    /*
        Provides a singleton ResponseSpecification instance expecting status code 200
        and JSON content type. Uses double-checked locking for thread safety.
     */
    public static ResponseSpecification responseSpecification() {
        if (resSpec == null) {
            synchronized (LOCK) {
                if (resSpec == null) {
                    resSpec = new ResponseSpecBuilder()
                            .expectStatusCode(200)
                            .expectContentType(ContentType.JSON)
                            .build();
                }
            }
        }
        return resSpec;
    }

    /*
        Reads the baseUrl from global.properties file.
     */
    private static String getGlobalValue(String key) throws FileNotFoundException {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/resources/global.properties")) {
            prop.load(fis);
            return prop.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load global.properties", e);
        }
    }

    public String getJsonPath(Response response, String key) {
        String resp = response.asString();
        JsonPath js = new JsonPath(resp);
        return js.getString(key);
    }
}
