@getTrainerTrainings
Feature: Get trainer trainings

    Scenario: Successful retrieval of trainer's trainings
        Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
        When a authenticated user tries to retrieve the trainings for trainer ID "john.doe"
        Then the response status should be 200
        And the response should contain a list of trainings for trainer ID "john.doe"

    Scenario: Trainer not found
        Given a trainer with ID "john.doe" exists with first name "John", last name "Doe"
        When a authenticated user tries to retrieve the trainings for a non-existing trainer ID "jane.doe"
        Then the response status should be 404

    Scenario: Unauthorized access
        When a user without proper authorization tries to retrieve the trainings for trainer ID "john.doe"
        Then the response status should be 403