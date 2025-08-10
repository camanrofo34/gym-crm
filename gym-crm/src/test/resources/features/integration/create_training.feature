Feature:  As a trainee, I want to create a training so that I can improve my skills and the trainer's workload is updated accordingly.

  Scenario: Create a training and verify trainer workload is updated
    Given a trainer "Jane.Doe" and a trainee "John.Doe" exist in the system
    When a training session is created with trainer "Jane.Doe" and trainee "John.Doe"
    Then the trainer workload should be updated in the receiving service

  Scenario: Create a training with invalid data
    Given a trainer "Jane.Doe" and a trainee "John.Doe" exist in the system
    When a training session is created without a date
    Then the response status should be 400
    And the response message should contain "{\"trainingDate\":\"Training date cannot be blank\"}"

  Scenario: Create a training without login before
    Given a trainer "Jane.Doe" and a trainee "John.Doe" exist in the system
    When a training session is created without the trainee being logged in
    Then the response status should be 403