@registerTrainee
Feature: Register Trainee

  Scenario: Successful trainee registration
    When a user tries to register a trainee with first name "john", last name "doe", birthdate "1900-01-01", and address "123 Main St"
    Then the response status should be 201


  Scenario: Missing required fields
    When a user tries to register as a trainee without providing first name, last name, birthdate, or address
    Then the response status should be 500