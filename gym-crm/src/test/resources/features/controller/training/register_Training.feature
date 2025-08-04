@registerTraining
Feature: Register training
    Scenario: Successful training registration
        Given a training type with ID "yoga" exists with name "Yoga"
        And a user with ID "alice.smith" exists with first name "Alice", last name "Smith"
        When an authenticated user tries to register for the training type with ID "yoga" for user "user123"
        Then the response status should be 201

    Scenario: Training registration for non-existent user
        Given a training type with ID "yoga" exists with name "Yoga"
        When an authenticated user tries to register for the training type with ID "yoga" for non-existent user "nonexistent_user"
        Then the response status should be 404

    Scenario: Unauthorized access to register for training
        Given a training type with ID "yoga" exists with name "Yoga"
        When a user without proper authorization tries to register for the training type with ID "yoga"
        Then the response status should be 403
