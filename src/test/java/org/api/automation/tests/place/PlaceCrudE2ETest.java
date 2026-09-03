package org.api.automation.tests.place;

import org.api.automation.base.BaseTest;
import org.api.automation.config.ConfigKeys;
import org.api.automation.config.ConfigManager;
import org.api.automation.constants.Paths;
import org.api.automation.data.TestDataFactory;
import org.api.automation.models.request.AddPlaceRequest;
import org.api.automation.models.request.DeletePlaceRequest;
import org.api.automation.models.request.UpdatePlaceRequest;
import org.api.automation.models.response.AddPlaceResponse;
import org.api.automation.models.response.GetPlaceResponse;
import org.api.automation.models.response.MessageResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * The full CRUD lifecycle: POST -> GET -> PUT -> GET -> DELETE.
 *
 * <p>Each step uses {@code dependsOnMethods} so a failure skips downstream steps rather
 * than producing a chain of misleading null-related failures.
 */
@Test(groups = {"e2e", "regression"})
public class PlaceCrudE2ETest extends BaseTest {

    private static final String UPDATED_ADDRESS = "70 Summer walk, USA";

    /** Carries the id between steps — instance field, not static. */
    private String placeId;

    @Test
    public void createPlace() {
        AddPlaceRequest request = TestDataFactory.addPlace(
                "Frontline House", "English", "29, side layout, cohen 09");

        AddPlaceResponse response = given(placesSpec())
                .body(request)
                .post(Paths.ADD_PLACE)
                .then().statusCode(200).extract().as(AddPlaceResponse.class);

        assertEquals(response.getStatus(), "OK", "Add Place status");
        assertNotNull(response.getPlaceId(), "place_id");

        placeId = response.getPlaceId();
        log.info("Created place {}", placeId);
    }

    @Test(dependsOnMethods = "createPlace")
    public void createdPlaceShouldBeRetrievableWithSubmittedValues() {
        GetPlaceResponse place = given(placesSpec())
                .queryParam("place_id", placeId)
                .get(Paths.GET_PLACE)
                .then().statusCode(200).extract().as(GetPlaceResponse.class);

        assertEquals(place.getName(), "Frontline House", "Stored name");
        assertEquals(place.getAddress(), "29, side layout, cohen 09", "Stored address");
    }

    @Test(dependsOnMethods = "createdPlaceShouldBeRetrievableWithSubmittedValues")
    public void updateShouldChangeAddress() {
        UpdatePlaceRequest request = new UpdatePlaceRequest(
                placeId, UPDATED_ADDRESS, ConfigManager.getRequired(ConfigKeys.PLACES_API_KEY));

        MessageResponse response = given(placesSpec())
                .body(request)
                .put(Paths.UPDATE_PLACE)
                .then().statusCode(200).extract().as(MessageResponse.class);

        assertEquals(response.getMessage(), "Address successfully updated", "Update acknowledgement");
    }

    @Test(dependsOnMethods = "updateShouldChangeAddress")
    public void updatedAddressShouldPersist() {
        GetPlaceResponse place = given(placesSpec())
                .queryParam("place_id", placeId)
                .get(Paths.GET_PLACE)
                .then().statusCode(200).extract().as(GetPlaceResponse.class);

        assertEquals(place.getAddress(), UPDATED_ADDRESS,
                "Address should reflect the update, not the original value");
    }

    @Test(dependsOnMethods = "updatedAddressShouldPersist", alwaysRun = true)
    public void deletePlace() {
        if (placeId == null) {
            throw new org.testng.SkipException("No place was created, nothing to delete");
        }
        MessageResponse response = given(placesSpec())
                .body(new DeletePlaceRequest(placeId))
                .delete(Paths.DELETE_PLACE)
                .then().statusCode(200).extract().as(MessageResponse.class);

        assertEquals(response.getStatus(), "OK", "Delete status");
    }
}
