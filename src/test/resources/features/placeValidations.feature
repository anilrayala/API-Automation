@Places
Feature: Google Places API

  Validating the create, retrieve and delete operations of the Places API.

  # A tag on the Feature applies to every Scenario in it, so everything here is @Places.
  # Filter at run time with:
  #   mvn test -Dcucumber.filter.tags="@AddPlace"
  #   mvn test -Dcucumber.filter.tags="@Regression and not @DeletePlace"

  @AddPlace @Regression @Smoke
  Scenario Outline: A place can be created and retrieved with the values submitted
    Given Add Place Payload with "<name>" "<language>" "<address>"
    When User calls "ADD_PLACE" API with "POST" http request
    Then API call is successful with status code 200
    And "status" in response body is "OK"
    And "scope" in response body is "APP"
    Then place_Id is captured from the response
    And the created place can be retrieved with name "<name>"
    And the created place has address "<address>"

    # All three rows are active. In the old feature file two of them were commented out,
    # which was hiding a real defect: the shared request specification accumulated
    # place_id query parameters between scenarios, so the second row would have failed.
    Examples:
      | name           | language | address     |
      | Frontline      | English  | 123 Main St |
      | Backline       | Spanish  | 456 Elm St  |
      | Sidewalk       | French   | 789 Oak St  |

  @DeletePlace @Regression
  Scenario: A place can be deleted
    # The place this scenario deletes is created by the @DeletePlace hook in Hooks.java,
    # freshly for each run. The old version relied on a place left behind by the Add Place
    # scenario via a static field, which meant the two scenarios could not run independently
    # or in either order.
    Given Delete Place Payload with the created place_Id
    When User calls "DELETE_PLACE" API with "DELETE" http request
    Then API call is successful with status code 200
    And "status" in response body is "OK"
