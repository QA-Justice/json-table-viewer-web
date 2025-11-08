Feature: Export functionality
Background:
  Given I open the JSON viewer page

#  @runonly
  Scenario: Export CSV file
    When I click the Convert button
    And I click the Export CSV button
    Then I should see a success notification
#  @runonly
  Scenario: Export CSV without data should show error
    When I click the Export CSV button
    Then I should see an error notification
#  @runonly
  Scenario: Export CSV after pivot
    When I click the Convert button
    And I click the Pivot button
    And I click the Export CSV button
    Then I should see a success notification
