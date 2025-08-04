@getTrainee
Feature: Get Trainee

  Scenario: Successful trainee retrieval
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to retrieve the trainee information for ID "john.doe"
    Then the response status should be 200

  Scenario: Trainee not found
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to retrieve the trainee information for ID "john.doe" but trainee does not exist
    Then the response status should be 404

  Scenario: Unauthorized access
    When a user without proper authorization tries to retrieve the trainee information for ID "john.doe"
    Then the response status should be 403