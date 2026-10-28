Feature: UI state management
Background:
  Given I open the JSON viewer page
  Scenario: Initial state should show no data message
    Then I should see the no data message
    And the table should be empty
  Scenario: Data state after conversion
    When I click the Convert button
    Then I should not see the no data message
    And the table should not be empty
  Scenario: Reset should return to initial state
    When I click the Convert button
    And I click the Reset button
    Then I should see the no data message
    And the table should be empty
  Scenario: Pivot button state changes
    When I click the Convert button
    Then the Pivot button should show "Pivot"
    When I click the Pivot button
    Then the Pivot button should show "Restore"
    When I click the Pivot button
    Then the Pivot button should show "Pivot"
