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

  # 표가 길면 표 영역 안에서 스크롤된다. 끝까지 내려도 헤더는 맨 위에 붙어 있어야 한다
  Scenario: Long table scrolls to the last row with a sticky header
    When I import the file "many-rows.json"
    Then I should see a success notification
    When I scroll to the bottom of the table
    Then the last table row should show "50"
    And the table header should stay at the top of the table
