@ChangePassword
Feature: Change user password

  Scenario: Successful password change
    Given a user with username "trainee" and password "1234" is authenticated
    When the user attempts to change the password with old password "1234" and new password "5678"
    Then the password change response status should be 200
