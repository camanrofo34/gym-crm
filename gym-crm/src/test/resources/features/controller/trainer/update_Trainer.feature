@updateTrainer
Feature: As a trainer, I want to update my profile so that I can keep my information current.

  Scenario: Successful update of trainer profile
    Given a trainer with ID "alice.johnson" exists with first name "Alice", last name "Johnson" and specialization "Yoga"
    When the trainer with ID "alice.johnson" updates their profile with first name "Alice", last name "Johnson", and specialization "Pilates"
    Then the response status should be 200
    And the update response should contain the trainer's ID "alice.johnson"
    And the update response should contain the trainer's first name "Alice"

  Scenario: Unsuccessful update due to missing first name
    Given a trainer with ID "alice.johnson" exists with first name "Alice", last name "Johnson" and specialization "Yoga"
    When the trainer with ID "alice.johnson" updates their profile with first name "", last name "Johnson", and specialization "Pilates"
    Then the response status should be 400
    And the response message should contain "{\"firstName\":\"Firstname cannot be blank\"}"

  Scenario: Unsuccessful update due to non-existent trainer
    Given a trainer with ID "alice.johnson" exists with first name "Alice", last name "Johnson" and specialization "Yoga"
    When the trainer with ID "non.existent" updates their profile with first name "Alice", last name "Johnson", and specialization "Pilates"
    Then the response status should be 404
    And the response message should contain "Trainer not found"

  Scenario: Unsuccessful update due to unauthorized access
    Given a trainer with ID "alice.johnson" exists with first name "Alice", last name "Johnson" and specialization "Yoga"
    When the trainer with ID "alice.johnson" updates their profile with first name "Alice", last name "Johnson", and specialization "Pilates" but the trainer has not logged in before
    Then the response status should be 403
    And the response message should contain "Unauthorized access"

  Scenario: Unsuccessful update due to non-existent specialization
    Given a trainer with ID "alice.johnson" exists with first name "Alice", last name "Johnson" and specialization "Yoga"
    When the trainer with ID "alice.johnson" updates their profile with first name "Alice", last name "Johnson", and specialization "NonExistentSpecialization"
    Then the response status should be 404
    And the response message should contain "Specialization not found"