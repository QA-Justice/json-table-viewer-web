Feature: JSON Table Rendering

  Scenario: Flatten simple JSON array
    Given I open the JSON viewer page
    When I click the Convert button
    Then I should see a table with 2 rows and 2 columns
#    And the column headers should include "id" and "name"
