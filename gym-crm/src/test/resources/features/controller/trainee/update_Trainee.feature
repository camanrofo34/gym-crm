@updateTrainee
Feature: As a trainee, I want to be able to update my information so that I can keep my profile details current.

  Scenario: Successful trainee account update
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to update the trainee information for ID "john.doe" with first name "John", last name "Doe", birthdate 1992-02-02, and address "456 Elm St"
    Then the response status should be 200
    And the trainee information should be updated to first name "John", last name "Doe", birthdate 1992-02-02, and address "456 Elm St"

  Scenario: Unsuccessful trainee account update due to missing address new information
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to update the trainee information for ID "john.doe" with first name "", last name "Doe", birthdate 1992-02-02, and address ""
    Then the response status should be 400
    And the response message should contain "{\"firstName\":\"First name cannot be blank\"}"

  Scenario: Unsuccessful trainee account update due to non-existent trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to update the trainee information for ID "jane.doe" with first name "Jane", last name "Doe", birthdate 1992-02-02, and address "456 Elm St"
    Then the response status should be 404
    And the response message should contain "Trainee not found"

  Scenario: Unsuccessful trainee account update due to missing previous authorization
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" tries to update the trainee information for ID "john.doe" with first name "John", last name "Doe", birthdate 1992-02-02, and address "456 Elm St" but the trainee has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"