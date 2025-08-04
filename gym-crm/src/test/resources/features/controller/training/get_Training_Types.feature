@getTrainingTypes
Feature: Get Training Types

  Scenario: Successfully get training types
    Given the following training types exist:
      | id | name          |
      | 1  | Basic Training|
      | 2  | Advanced Training|
    When I send a GET request to "/training/trainingTypes"
    Then the response status code should be 200
    And the response should contain the following training types:
      | id | name              |
      | 1  | Basic Training    |
      | 2  | Advanced Training  |

  Scenario: Unauthorized access to get training types
    When I send a GET request to "/training/trainingTypes" without authorization
    Then the response status code should be 403