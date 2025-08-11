@activateDeactivateTrainee
Feature: As a trainee, I want to be able to activate or deactivate my account so that I can control my participation in the gym's programs.

  Scenario: Successful activation of a trainee account.
    Given a trainee with ID "john.doe" exists in the system
    When a trainee with ID "john.doe" tries to activate their account
    Then the response status should be 200
    And the trainee with ID "john.doe" should be active

  Scenario: Successful deactivation of a trainee account
    Given a trainee with ID "john.doe" exists in the system
    When a trainee with ID "john.doe" tries to deactivate their account
    Then the response status should be 200
    And the trainee with ID "john.doe" should be inactive

  Scenario: Unauthorized access to try to activate a trainee account
    Given a trainee with ID "john.doe" exists in the system
    When a trainee with ID "john.doe" tries to activate their account but the trainee has not login before
    Then the response status should be 200
    And the trainee with ID "john.doe" should be inactive

  Scenario: Unauthorized access to deactivate trainee
    Given a trainee with ID "john.doe" exists in the system
    When a trainee with ID "john.doe" tries to deactivate their account but the trainee has not login before
    Then the response status should be 200
    And the trainee with ID "john.doe" should be active