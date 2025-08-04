@updateTrainee
Feature: Update Trainee

  Scenario: Successful trainee update
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to update the trainee information with first name "John", last name "Doe", birthdate "1990-01-01", and address "123 Main St"
    Then the update response status should be 200
    And the trainee information should be updated to first name "John", last name "Smith"

  Scenario: Unsuccessful trainee update due to missing required fields
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a authenticated user tries to update the trainee information without providing first name, last name, birthdate, or address
    Then the update response status should be 500

  Scenario: Unauthorized access to update trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    When a user without proper authorization tries to update the trainee information
    Then the update response status should be 403