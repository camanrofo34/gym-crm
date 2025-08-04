@registerTrainer
Feature: Register a new Trainer

  Scenario: Successful trainer registration
    When a user tries to register a trainer with first name "john", last name "doe" and specialization with id 1
    Then the response status should be 201

  Scenario: Unsuccessful trainer registration due to missing first name
    When a user tries to register a trainer with last name "doe" and specialization with id 1
    Then the response status should be 500