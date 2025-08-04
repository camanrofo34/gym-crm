@activateDeactivateTrainee
Feature: Activa Deactive Trainee

  Scenario: Successful activation of trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to activate the trainee with ID "john.doe"
    Then the activation response status should be 200
    And the trainee with ID "john.doe" should be active

  Scenario: Successful deactivation of trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to deactivate the trainee with ID "john.doe"
    Then the deactivation response status should be 200
    And the trainee with ID "john.doe" should be inactive

  Scenario: Unauthorized access to activate trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to activate the trainee with ID "john.doe"
    Then the activation response status should be 403

  Scenario: Unauthorized access to deactivate trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to deactivate the trainee with ID "john.doe"
    Then the deactivation response status should be 403