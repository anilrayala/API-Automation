package org.api.automation.tests.place;

import org.api.automation.base.BaseTest;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.response.AddPlaceResponse;
import org.api.automation.models.response.GetPlaceResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Parameterised Add Place, driven by a TestNG {@code @DataProvider}.
 *
 * <p>A provider returns {@code Object[][]} — one row per execution. TestNG runs the method
 * once per row and reports each as a separate result.
 */
public class AddPlaceDataDrivenTest extends BaseTest {

    @DataProvider(name = "places")
    public Object[][] places() {
        return new Object[][]{
                {"Frontline House", "English", "123 Main St"},
                {"Backline House", "Spanish", "456 Elm St"},
                {"Sidewalk House", "French", "789 Oak St"}
        };
    }

    @Test(dataProvider = "places", groups = {"regression", "datadriven"})
    public void shouldCreateAndRetrievePlace(String name, String language, String address) {
        AddPlaceResponse created = given(placesSpec())
                .body(TestDataFactory.addPlace(name, language, address))
                .post(Paths.ADD_PLACE)
                .then().statusCode(200).extract().as(AddPlaceResponse.class);

        assertEquals(created.getStatus(), "OK", "Add Place status for " + name);
        assertNotNull(created.getPlaceId(), "place_id for " + name);

        GetPlaceResponse fetched = given(placesSpec())
                .queryParam("place_id", created.getPlaceId())
                .get(Paths.GET_PLACE)
                .then().statusCode(200).extract().as(GetPlaceResponse.class);

        assertEquals(fetched.getName(), name, "Round-tripped name for row " + name);
        assertEquals(fetched.getAddress(), address, "Round-tripped address for row " + name);

        given(placesSpec())
                .body(new org.api.automation.models.request.DeletePlaceRequest(created.getPlaceId()))
                .delete(Paths.DELETE_PLACE);
    }

    /**
     * A provider that generates unique names — shows that data providers can be dynamic.
     * Required here because the API rejects duplicate place names.
     */
    @DataProvider(name = "generatedPlaces")
    public Object[][] generatedPlaces() {
        int rows = 2;
        Object[][] data = new Object[rows][3];
        for (int i = 0; i < rows; i++) {
            data[i] = new Object[]{
                    "Generated Place " + TestDataFactory.uniqueSuffix(),
                    "English",
                    (100 + i) + " Generated Street"
            };
        }
        return data;
    }

    @Test(dataProvider = "generatedPlaces", groups = {"regression", "datadriven"})
    public void shouldCreatePlaceWithGeneratedData(String name, String language, String address) {
        AddPlaceResponse created = given(placesSpec())
                .body(TestDataFactory.addPlace(name, language, address))
                .post(Paths.ADD_PLACE)
                .then().statusCode(200).extract().as(AddPlaceResponse.class);

        assertEquals(created.getStatus(), "OK", "Add Place status for " + name);

        given(placesSpec())
                .body(new org.api.automation.models.request.DeletePlaceRequest(created.getPlaceId()))
                .delete(Paths.DELETE_PLACE);
    }
}
