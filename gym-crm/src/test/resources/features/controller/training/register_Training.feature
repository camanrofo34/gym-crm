@registerTraining
Feature: As a trainee, I want to register for a training so that I can improve my skills.

  Scenario: Successful registration for a training
    Given the following training types exist:
      | id | name              |
      | 1  | Basic Training    |
      | 2  | Advanced Training  |
    And the trainee with ID "john.doe" exists
    And the trainer with ID "jane.smith" exists
    When the trainee with ID "john.doe" registers for training type "Basic Training", with date 2023-10-01, trainer ID "jane.smith" and duration 20.0 minutes
    Then the response status should be 201

  Scenario: Unsuccessful registration due to missing trainer ID
    Given the following training types exist:
      | id | name              |
      | 1  | Basic Training    |
      | 2  | Advanced Training  |
    And the trainee with ID "john.doe" exists
    And the trainer with ID "jane.smith" exists
    When the trainee with ID "john.doe" registers for training type "Basic Training", with date 2023-10-01, trainer ID "" and duration 20.0 minutes
    Then the response status should be 400
    And the response message should contain "{\"trainerUsername\":\"Trainer username cannot be blank\"}"


