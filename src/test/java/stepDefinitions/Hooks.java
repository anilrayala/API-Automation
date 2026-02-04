package stepDefinitions;


import io.cucumber.java.Before;

import java.io.FileNotFoundException;

public class Hooks {

    @Before("@DeletePlace")
    public void beforeScenario() throws FileNotFoundException {
        // Code to execute before each scenario
        System.out.println("Before Scenario: Setting up preconditions.");
        StepDefinition stepDef = new StepDefinition();
        if (StepDefinition.placeId == null) {
            stepDef.addPlacePayloadWith("DefaultName", "English", "123 Default St");
            stepDef.user_calls_api_with_http_request("ADD_PLACE", "POST");
            stepDef.verify_place_id_created_maps_to_using("DefaultName", "GET_PLACE");
        }
    }
}
