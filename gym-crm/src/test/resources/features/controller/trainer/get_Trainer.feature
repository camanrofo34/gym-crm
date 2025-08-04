@getTrainer
Feature: Get Trainer

  Scenario: Successful trainer retrieval
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to retrieve the trainer information for ID "john.doe"
    Then the response status should be 200

  Scenario: Trainer not found
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to retrieve the trainer information for ID "john.doe" but trainee does not exist
    Then the response status should be 404

  Scenario: Unauthorized access
    When a user without proper authorization tries to retrieve the trainer information for ID "john.doe"
    Then the response status should be 403