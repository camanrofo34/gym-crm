Feature: As an trainer, I wish to register in the platform and have a unique ID, so I can manage my training sessions and workload effectively.

  Scenario: Successful trainer registration
    Given a trainer "Jane.Doe" and a trainee "John.Doe" exist in the system
    When the new trainer registers with first name "Jane", last name "Doe" and specialization "YOGA"
    Then the response status should be 201
    And the response should contain the trainer's unique ID "Jane.Doe4"

  Scenario: Unsuccessful trainer registration due to incorrect training type
    Given a trainer "Jane.Doe" and a trainee "John.Doe" exist in the system
    When the new trainer registers with first name "Jane", last name "Doe" and specialization "INVALID_TYPE"
    Then the response status should be 404
    And the response message should contain "Training type with id: 999 not found"