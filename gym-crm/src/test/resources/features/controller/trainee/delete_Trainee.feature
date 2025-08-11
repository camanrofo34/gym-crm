@deleteTrainee
Feature: As a trainee, I want to be able to delete my account so that I can remove my profile from the system.

  Scenario: Successful trainee account deletion
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to delete his trainee account with ID "john.doe"
    Then the response status should be 204
    And the trainee with ID "john.doe" should no longer exist

  Scenario: Unsuccessful trainee deletion due to non-existent ID
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to delete a trainee account with ID "jane.doe"
    Then the response status should be 404
    And the response message should contain "Trainee not found"

  Scenario: Unsuccessful trainee deletion due to missing authorization
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to delete a trainee account with ID "john.doe" but the trainee has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"