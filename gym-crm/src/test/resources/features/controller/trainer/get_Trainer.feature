@getTrainer
Feature: As a trainer, I want to be able to retrieve my information so that I can view my profile details.

  Scenario: Successful trainer retrieval
    Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
    When the trainer with ID "jane.smith" retrieve the trainer information for ID "jane.smith"
    Then the response status should be 200
    And the response should contain the trainer's first name "Jane"

  Scenario: Unsuccessful trainer retrieval due to non-existent trainer
    Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
    When the trainer with ID "jane.smith" retrieve the trainer information for ID "john.doe"
    Then the response status should be 404
    And the response message should contain "Trainer not found"

  Scenario: Unsuccessful trainer retrieval due to missing authorization
    Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
    When the trainer with ID "jane.smith" retrieve the trainer information for ID "jane.smith" but the trainer has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"