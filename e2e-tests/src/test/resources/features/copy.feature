Feature: Copy to clipboard functionality
Background:
  Given I open the JSON viewer page

  Scenario: Copy data to clipboard in markdown format
    When I click the Convert button
    And I click the Copy button
    And I select copy format "markdown"
    Then the clipboard should contain:
      """
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |
      | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
      | John Doe | 30 | john@example.com | true | 85 | 92 | 78 | 123 Main St | New York | 10001 |
      | Jane Smith | 25 | jane@example.com | false | 90 | 88 | 95 | 456 Oak Ave | Los Angeles | 90210 |
      | Bob Johnson | 35 | bob@example.com | true | 75 | 82 | 88 | 789 Pine Rd | Chicago | 60601 |
      """

  Scenario: Copy data to clipboard in text format
    When I click the Convert button
    And I click the Copy button
    And I select copy format "text"
    Then the clipboard should contain tab-separated rows:
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |
      | John Doe | 30 | john@example.com | true | 85 | 92 | 78 | 123 Main St | New York | 10001 |
      | Jane Smith | 25 | jane@example.com | false | 90 | 88 | 95 | 456 Oak Ave | Los Angeles | 90210 |
      | Bob Johnson | 35 | bob@example.com | true | 75 | 82 | 88 | 789 Pine Rd | Chicago | 60601 |

  # 셀 안의 | 가 \| 로 이스케이프되지 않으면 마크다운 표의 열이 깨진다
  Scenario: Markdown copy escapes pipe characters
    When I enter JSON data:
      """
      [{"name": "A|B", "note": "plain"}]
      """
    And I click the Convert button
    And I click the Copy button
    And I select copy format "markdown"
    Then the clipboard should contain:
      """
      | name | note |
      | --- | --- |
      | A\|B | plain |
      """

  Scenario: Copy without data should show error
    When I click the Copy button
    And I select copy format "markdown"
    Then I should see the error "No data to copy. Please convert JSON first."
