@getTrainingTypes
Feature: As a trainee, I want to view available training types so that I can choose the right one for my needs.

  Scenario: Successfully retrieving all training types
    Given the following training types exist:
      | id | name              |
      | 1  | Basic Training    |
      | 2  | Advanced Training |
    When the trainee requests all training types
    Then the response status should be 200
    And the response should contain the training type with ID "1" and name "Basic Training"
    And the response should contain the training type with ID "2" and name "Advanced Training"

  Scenario: Unsuccessfully retrieving due to unauthorized access
    Given the following training types exist:
      | id | name              |
      | 1  | Basic Training    |
      | 2  | Advanced Training |
    When the trainee requests all training types but the trainee has not logged in
    Then the response status should be 403
    And the response message should contain "Unauthorized access"