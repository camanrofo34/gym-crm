@deleteTrainee
Feature: Delete Trainee

  Scenario: Successful trainee deletion
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to delete the trainee with ID "john.doe"
    Then the delete response status should be 204
    And the trainee with ID "john.doe" should no longer exist

  Scenario: Unsuccessful trainee deletion due to non-existent ID
    Given a trainee with ID "john.doe" does not exist
    When a authenticated user tries to delete the trainee with ID "john.doe" but trainee does not exist
    Then the delete response status should be 404

  Scenario: Unauthorized access to delete trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to delete the trainee with ID "john.doe"
    Then the delete response status should be 403