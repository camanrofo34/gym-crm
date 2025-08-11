@getTrainerTrainings
Feature: As a Trainer, I want to retrieve my trainings so that I can manage them.

    Scenario: Successful retrieval of trainings for trainer
        Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
        And trainings with IDs "trainingA", "trainingB" exist and are assigned to trainer with ID "jane.smith"
        When the trainer with ID "jane.smith" retrieves their trainings
        Then the response status should be 200
        And the response should contain training IDs "trainingA", "trainingB"

    Scenario: Unsuccessful retrieval due to non-existent trainer
        Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
        And trainings with IDs "trainingA", "trainingB" exist and are assigned to trainer with ID "jane.smith"
        When the trainer with ID "trainer.john" retrieves their trainings
        Then the response status should be 404
        And the response message should contain "Trainer not found"

    Scenario: Unsuccessful retrieval due to unauthorized access
        Given a trainer with ID "jane.smith" exists with first name "Jane", last name "Smith" and specialization "Yoga"
        And trainings with IDs "trainingA", "trainingB" exist and are assigned to trainer with ID "jane.smith"
        When the trainer with ID "jane.smith" retrieves their trainings but the trainer has not logged in before
        Then the response status should be 403
        And the response message should contain "Unauthorized access"