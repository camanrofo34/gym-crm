@assignTrainersToTrainee
Feature: Assing Trainers to Trainee

  Scenario: Successful assignment of trainers to trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    And trainers with IDs "trainer1", "trainer2" exist
    When a authenticated user tries to assign trainers with IDs "trainer1", "trainer2" to the trainee with ID "john.doe"
    Then the assignment response status should be 200
    And the trainee with ID "john.doe" should have trainers with IDs "trainer1", "trainer2" assigned

  Scenario: Unsuccessful assignment due to non-existent trainee
    Given a trainee with ID "john.doe" does not exist
    And trainers with IDs "trainer1", "trainer2" exist
    When a authenticated user tries to assign trainers with IDs "trainer1", "trainer2" to the trainee with ID "john.doe" but trainee does not exist
    Then the assignment response status should be 404

  Scenario: Unauthorized access to assign trainers to trainee
    Given a trainee with ID "john.doe" exists with first name "John", last name "Doe"
    And trainers with IDs "trainer1", "trainer2" exist
    When a user without proper authorization tries to assign trainers with IDs "trainer1", "trainer2" to the trainee with ID "john.doe"
    Then the assignment response status should be 403