Feature: Copy to clipboard functionality
Background:
  Given I open the JSON viewer page

  @runonly
  Scenario: Copy data to clipboard in markdown format
    When I click the Convert button
    And I click the Copy button
    And I select copy format "markdown"
    Then I should see a success notification
  @runonly
  Scenario: Copy data to clipboard in text format
    When I click the Convert button
    And I click the Copy button
    And I select copy format "text"
    Then I should see a success notification
  @runonly
  Scenario: Copy without data should show error
    When I click the Copy button
    And I select copy format "markdown"
    Then I should see an error notification
