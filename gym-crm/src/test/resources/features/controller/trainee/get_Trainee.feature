@getTrainee
Feature: As an trainee, I want to be able to retrieve my information so that I can view my profile details.

  Scenario: Successful trainee retrieval
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" retrieve the trainee information for ID "john.doe"
    Then the response status should be 200
    And the response should contain the trainee's first name "John"

  Scenario: Unsuccessful trainee retrieval due to non-existent trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" retrieve the trainee information for ID "jane.doe"
    Then the response status should be 404
    And the response message should contain "Trainee not found"

  Scenario: Unsuccessful trainee retrieval due to missing authorization
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" retrieve the trainee information for ID "john.doe" but the trainee has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"

  Scenario: Unsuccessful trainee retrieval due an internal database error
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    When the trainee with ID "john.doe" retrieve the trainee information for ID "john.doe" but there is a database error
    Then the response status should be 500
    And the response message should contain "Database connection error: Data is not accessible in this moment"
