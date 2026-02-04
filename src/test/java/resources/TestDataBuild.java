package resources;

import pojo.AddPlace;
import pojo.Location;

import java.util.ArrayList;
import java.util.List;

public class TestDataBuild {

    public AddPlace addPlacePayload(String name, String language, String address) {
        // Method to build and return the payload for adding a place
        AddPlace addPlace = new AddPlace();
        addPlace.setAccuracy(50);
        addPlace.setAddress(address);
        addPlace.setLanguage(language);
        addPlace.setName(name);
        addPlace.setPhone_number("(+91) 983 893 3937");
        addPlace.setWebsite("https://rahulshettyacademy.com");

        List<String> types = new ArrayList<>();
        types.add("shoe park");
        types.add("shop");
        addPlace.setTypes(types);

        Location location = new Location();
        location.setLat(-38.383494);
        location.setLng(33.427362);
        addPlace.setLocation(location);

        return addPlace;
    }

    public String deletePlacePayload(String placeId) {
        // Method to build and return the payload for deleting a place
        return "{\n" +
                "    \"place_id\":\""+placeId+"\"\n" +
                "}";
    }
}
