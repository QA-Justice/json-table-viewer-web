Feature: JSON validation and error handling
Background:
  Given I open the JSON viewer page
#  @runonly
  Scenario: Convert invalid JSON should show error
    When I enter invalid JSON data
    And I click the Convert button
    Then I should see a JSON parse error
#  @runonly
  Scenario: Convert empty JSON should show error
    When I clear the JSON input
    And I click the Convert button
    Then I should see an error notification

#  @runonly
  Scenario: Reset to sample data
    When I clear the JSON input
    And I click the Reset button
    And I click the Convert button
    Then the table headers should be:
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |

#  @runonly
  Scenario: Keyboard shortcut for convert
    When I enter JSON data:
      """
      [{"name": "Test", "age": 25}]
      """
    And I press Ctrl+Enter in the JSON input
    Then the table headers should be:
      | name | age |
