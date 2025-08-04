@activateDeactivateTrainer
Feature: Activate/Deactivate Trainer

  Scenario: Successful trainer activation
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to activate the trainer with ID "john.doe"
    Then the activation response status should be 200
    And the trainer should be active

  Scenario: Successful trainer deactivation
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to deactivate the trainer with ID "john.doe"
    Then the deactivation response status should be 200
    And the trainer should be inactive

  Scenario: Unauthorized access to activate/deactivate trainer
    Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to activate deactivate the trainer with ID "john.doe"
    Then the response status should be 403