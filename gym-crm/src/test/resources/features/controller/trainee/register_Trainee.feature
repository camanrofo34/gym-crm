@registerTrainee
Feature: As a new trainee, I want to be able to register so that I can start using the gym's services.

  Scenario: Successful trainee account registration in the system
    When a user tries to register a trainee with first name "john", last name "doe", birthdate 1900-01-01, and address "123 Main St"
    Then the response status should be 201


  Scenario: Unsuccessful trainee account registration due to missing first name
    When a user tries to register a trainee with first name "", last name "doe", birthdate 1900-01-01, and address "123 Main St"
    Then the response status should be 400
    And the response message should contain "{\"firstName\":\"First name cannot be blank\"}"