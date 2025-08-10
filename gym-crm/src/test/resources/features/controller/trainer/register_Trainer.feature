@registerTrainer
Feature: As a new trainer, I want to register so that I can start managing my trainings.

  Scenario: Successful registration of a new trainer
    When the new trainer registers with first name "Alice", last name "Johnson" and specialization "Pilates"
    Then the response status should be 201
    And the response should contain as username "alice.johnson"

  Scenario: Unsuccessful registration due to missing first name
    When the new trainer registers with first name "", last name "Johnson" and specialization "Pilates"
    Then the response status should be 400
    And the response message should contain "{\"firstName\":\"Firstname cannot be blank\"}"

  Scenario: Unsuccessful registration due to specialization not found
    When the new trainer registers with first name "Alice", last name "Johnson" and specialization "UnknownSpecialization"
    Then the response status should be 404
    And the response message should contain "Specialization not found"