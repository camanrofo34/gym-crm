@updateTrainer
Feature: Update Trainer

  Scenario: Successful trainer update
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to update the trainer information with first name "John", last name "Doe", birthdate "1990-01-01", and address "123 Main St"
    Then the update response status should be 200
    And the trainer information should be updated to first name "John", last name "Smith"

  Scenario: Unsuccessful trainer update due to missing required fields
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to update the trainer information without providing first name, last name, birthdate, or address
    Then the update response status should be 500

  Scenario: Unauthorized access to update trainer
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to update the trainer information
    Then the update response status should be 403