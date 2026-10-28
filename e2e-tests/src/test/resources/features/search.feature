Feature: Table search functionality
Background:
  Given I open the JSON viewer page

  Scenario: Open search with Ctrl+F
    When I click the Convert button
    And I press Ctrl+F
    Then the search box should be visible
  # 검색은 th(컬럼명)와 td(셀)를 모두 훑고, 일치하는 요소마다 하이라이트를 하나씩 만든다.
  # 아래 개수는 샘플 JSON 기준 실측값이다. "John" → John Doe / john@example.com / Bob Johnson
  Scenario: Search for text in table
    When I click the Convert button
    And I press Ctrl+F
    And I type "John" in the search box
    Then I should see 3 highlighted search results
    And I should see 1 current match highlights
    And the search count should show "1/3"
  Scenario: Navigate through search results
    When I click the Convert button
    And I press Ctrl+F
    And I type "a" in the search box
    Then I should see 16 highlighted search results
    And the search count should show "1/16"
    When I click the search next button
    Then the search count should show "2/16"
    When I click the search previous button
    Then the search count should show "1/16"

  Scenario: Close search with Escape key
    When I click the Convert button
    And I press Ctrl+F
    And I type "test" in the search box
    And I press Escape key
    Then the search box should not be visible
  Scenario: Close search with close button
    When I click the Convert button
    And I press Ctrl+F
    And I click the search close button
    Then the search box should not be visible
  Scenario: Search with no results
    When I click the Convert button
    And I press Ctrl+F
    And I type "nonexistent" in the search box
    Then I should see 0 highlighted search results
    And the search count should show "0/0"
  Scenario: Search navigation with keyboard
    When I click the Convert button
    And I press Ctrl+F
    And I type "e" in the search box
    And I press Enter in the search box
    Then the search count should show "2/22"
    When I press Shift+Enter in the search box
    Then the search count should show "1/22"
