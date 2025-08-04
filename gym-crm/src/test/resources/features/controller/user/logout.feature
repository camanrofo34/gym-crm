@logout
Feature: User logout

  Scenario: Successful logout
    Given a user with username "trainer" and password "1234" is authenticated
    When the user attempts to logout
    Then the logout response status should be 200
    And the token should be added to the blacklist
