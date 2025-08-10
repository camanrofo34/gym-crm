@login
Feature: As a user of the gym CRM, I want to be able to log in so that I can access my account.

  Scenario: Successful login
    Given a user with username "trainer" and password "1234"
    When the user attempts to login with username "trainer" and password "1234"
    Then the login response status should be 200
    And a JWT token should be returned

  Scenario: Failed login due to wrong credentials
    Given a user with username "trainer" and password "1234"
    When the user attempts to login with username "trainer" and password "wrongpass"
    Then the login response status should be 403

  Scenario: User blocked after multiple failed attempts
    Given a user with username "trainer" and password "1234"
    When the user attempts to login with username "trainer" and password "wrongpass" six times
    Then the login response status should be 401