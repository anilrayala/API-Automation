package resources;

public enum APIResources {

    ADD_PLACE("/maps/api/place/add/json"),
    GET_PLACE("/maps/api/place/get/json"),
    DELETE_PLACE("/maps/api/place/delete/json");

    private final String resource;

    APIResources(String resource){
        this.resource = resource;
    }

    public String getResource(){
        return resource;
    }
}

