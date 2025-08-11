Feature: Process Trainer Workload Message

  Scenario: Process a valid workload update message
    Given a valid JWT token
    And a valid trainer workload message
    When the message is sent to the queue
    Then the trainer workload should be updated

  Scenario: Reject a message with an invalid token
    Given an invalid JWT token
    And a valid trainer workload message
    When the message is sent to the queue
    Then an invalid token error should be logged
