@getTrainersNotAssignedToTrainee
Feature: As a trainee, I want to retrieve trainers not assigned to me to manage my training sessions effectively.

  Scenario: Successfully retrieve trainers not assigned to trainee
    Given a trainee with ID "jane.smith" exists with first name "Jane", last name "Smith", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainerA", "trainerB", "trainerC" exist
    And the trainee with ID "jane.smith" is assigned to trainer with ID "trainerA"
    When the trainee with ID "jane.smith" tries to retrieve trainers not assigned to her
    Then the response status should be 200

  Scenario: Unsuccessful retrieval due to non-existent trainee
    Given a trainee with ID "jane.smith" exists with first name "Jane", last name "Smith", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainerA", "trainerB", "trainerC" exist
    And the trainee with ID "jane.smith" is assigned to trainer with ID "trainerA"
    When the trainee with ID "john.doe" tries to retrieve trainers not assigned to her
    Then the response status should be 404
    And the response message should contain "Trainee not found"

  Scenario: Unsuccessful retrieval due to missing authorization
    Given a trainee with ID "jane.smith" exists with first name "Jane", last name "Smith", birthdate 1990-01-01, and address "123 Main St"
    And trainers with IDs "trainerA", "trainerB", "trainerC" exist
    And the trainee with ID "jane.smith" is assigned to trainer with ID "trainerA"
    When the trainee with ID "jane.smith" tries to retrieve trainers not assigned to her but the trainee has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"