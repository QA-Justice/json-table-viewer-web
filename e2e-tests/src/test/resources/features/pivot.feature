Feature: Pivot table functionality
Background:
  Given I open the JSON viewer page

#  @runonly
  Scenario: Toggle pivot mode
    When I click the Convert button
    Then the Pivot button should show "Pivot"
    And the table should not be in pivot mode
    When I click the Pivot button
    Then the Pivot button should show "Restore"
    And the table should be in pivot mode
    When I click the Pivot button
    Then the Pivot button should show "Pivot"
    And the table should not be in pivot mode

#  @runonly
  Scenario: Pivot without data should show error
    When I click the Pivot button
    Then I should see an error notification
#  @runonly
  Scenario: Pivot table should have correct structure
    When I click the Convert button
    And I click the Pivot button
    Then the table headers should be:
      | Field | Row 1 | Row 2 | Row 3 |
