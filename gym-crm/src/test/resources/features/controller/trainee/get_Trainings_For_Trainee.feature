@GetTrainingsForTrainee
Feature: As an trainee, I want to be able to retrieve my training sessions so that I can view my schedule.

  Scenario: Successful retrieval of trainings for trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainings with IDs "training1", "training2" exist and are assigned to trainee with ID "john.doe"
    When the trainee with ID "john.doe" retrieves their trainings
    Then the response status should be 200

  Scenario: Unsuccessful retrieval due to non-existent trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainings with IDs "training1", "training2" exist and are assigned to trainee with ID "john.doe"
    When the trainee with ID "jane.doe" retrieves their trainings
    Then the response status should be 404
    And the response message should contain "Trainee not found"

  Scenario: Unsuccessful retrieval due to unauthorized access
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe", birthdate 1990-01-01, and address "123 Main St"
    And trainings with IDs "training1", "training2" exist and are assigned to trainee with ID "john.doe"
    When the trainee with ID "john.doe" retrieves their trainings but the trainee has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"