Feature: Validating Place API's

@AddPlace @Regression
Scenario Outline: Verify if place is added successfully using Add Place API
    Given Add Place Payload with "<name>" "<language>" "<address>"
    When User calls "ADD_PLACE" API with "POST" http request
    Then API call is successful with status code 200
    And "status" in response body is "OK"
    And "scope" in response body is "APP"
    Then verify place_Id created maps to "<name>" using "GET_PLACE" API

    Examples:
    | name        | language | address         |
    | Frontline   | English  | 123 Main St     |
#    | Backline    | Spanish  | 456 Elm St      |
#    | Sidewalk    | French   | 789 Oak St      |

@DeletePlace @Regression
Scenario: Verify if place is deleted successfully using Delete Place API
    Given Delete Place Payload with place_Id
    When User calls "DELETE_PLACE" API with "DELETE" http request
    Then API call is successful with status code 200
    And "status" in response body is "OK"

