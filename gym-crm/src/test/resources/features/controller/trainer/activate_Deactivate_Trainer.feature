@activateDeactivateTrainer
Feature: As a trainer, I want to be able to activate or deactivate my account so that I can control my participation in the gym's programs.

  Scenario: Successful activation of a trainer account.
    Given a trainer with ID "jane.smith" exists in the system
    When a trainer with ID "jane.smith" tries to activate their account
    Then the response status should be 200
    And the trainer with ID "jane.smith" should be active

  Scenario: Successful deactivation of a trainer account
    Given a trainer with ID "jane.smith" exists in the system
    When a trainer with ID "jane.smith" tries to deactivate their account
    Then the response status should be 200
    And the trainer with ID "jane.smith" should be inactive

  Scenario: Unauthorized access to try to activate a trainer account
    Given a trainer with ID "jane.smith" exists in the system
    When a trainer with ID "jane.smith" tries to activate their account but the trainer has not login before
    Then the response status should be 403
    And the trainer with ID "jane.smith" should be inactive

  Scenario: Unauthorized access to deactivate trainer
    Given a trainer with ID "jane.smith" exists in the system
    When a trainer with ID "jane.smith" tries to deactivate their account but the trainer has not login before
    Then the response status should be 403
    And the trainer with ID "jane.smith" should be active