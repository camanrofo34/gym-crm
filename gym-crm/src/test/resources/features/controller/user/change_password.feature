@ChangePassword
Feature: As a user, I want to change my password so that I can maintain the security of my account.

  Scenario: Successful password change
    Given a user with username "trainee" and password "1234"
    When the user attempts to change the password with old password "1234" and new password "5678"
    Then the password change response status should be 200

  Scenario: Unsuccessful password change due to incorrect old password
    Given a user with username "trainee" and password "1234"
    When the user attempts to change the password with old password "wrongpassword" and new password "5678"
    Then the password change response status should be 403
    And the response should contain "Old password is incorrect"

  Scenario: Unsuccessful password change due to unauthorized access
    Given a user with username "trainee" and password "1234"
    When the user attempts to change the password with old password "1234" and new password "5678" but the user has not logged in before
    Then the password change response status should be 403
    And the response should contain "Unauthorized access"
