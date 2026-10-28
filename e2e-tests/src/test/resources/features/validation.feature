Feature: JSON validation and error handling
Background:
  Given I open the JSON viewer page
  Scenario: Convert invalid JSON should show error
    When I enter invalid JSON data
    And I click the Convert button
    Then I should see an error containing "JSON parse error:"

  Scenario: Line-number gutter follows multiline JSON input
    When I enter JSON data:
      """
      {
        "name": "Test"
      }
      """
    Then the line-number gutter should show "1 2 3"

  Scenario: Parse error focuses and selects the invalid character
    When I enter JSON data:
      """
      {
        "name": "Test",
      }
      """
    And I click the Convert button
    Then I should see an error containing "JSON parse error:"
    And the JSON error position should be selected

  Scenario: Convert empty JSON should show error
    When I clear the JSON input
    And I click the Convert button
    Then I should see the error "Please enter JSON data."

  Scenario: Reset to sample data
    When I clear the JSON input
    And I click the Reset button
    And I click the Convert button
    Then the table headers should be:
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |

  Scenario: Keyboard shortcut for convert
    When I enter JSON data:
      """
      [{"name": "Test", "age": 25}]
      """
    And I press Ctrl+Enter in the JSON input
    Then the table headers should be:
      | name | age |
