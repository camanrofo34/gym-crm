Feature: Trainer workload is updated after training creation

  Scenario: Create a training and verify trainer workload is updated
    Given a trainer "jane.doe" and a trainee "john.doe" exist in the system
    When a training session is created with trainer "jane.doe" and trainee "john.doe"
    Then the trainer workload should be updated in the receiving service