@login
Feature: User login

  Scenario: Successful login
    Given a user with username "trainer" and password "1234"
    When the user attempts to login with username "trainer" and password "1234"
    Then the login response status should be 200
    And a JWT token should be returned

  Scenario: Failed login due to wrong credentials
    Given a user with username "trainer" and password "1234"
    When the user attempts to login with username "trainer" and password "wrongpass"
    Then the login response status should be 401
