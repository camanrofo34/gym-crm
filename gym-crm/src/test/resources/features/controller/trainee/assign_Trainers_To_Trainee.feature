@assignTrainersToTrainee
Feature: As a trainee, I want to be able to assign trainers to myself so that I can have multiple trainers.

  Scenario: Successful assignment of trainers to trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainer1", "trainer2" exist
    When the trainee with ID "john.doe" assigns trainers with IDs "trainer1", "trainer2" to themselves
    Then the response status should be 200
    And the assignment response message should contain trainers with IDs "trainer1", "trainer2"

  Scenario: Unsuccessful assignment due to non-existent trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainer1", "trainer2" exist
    When the trainee with ID "jane.doe" assigns trainers with IDs "trainer1", "trainer2" to themselves
    Then the response status should be 404
    And the assignment response message should contain "Trainee not found"

  Scenario: Unsuccessful assignment due to unauthorized access
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainer1", "trainer2" exist
    When the trainee with ID "john.doe" assigns trainers with IDs "trainer1", "trainer2" to themselves but the trainee has not logged in before
    Then the response status should be 403
    And the assignment response message should contain "Unauthorized access"