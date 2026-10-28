Feature: File upload functionality
Background:
  Given I open the JSON viewer page
  Scenario: Upload valid JSON file
    When I import the file "array-rooted.json"
    Then all column names should match the expected headers for "array-rooted"
    And each cell should match the expected table for "array-rooted"
  Scenario: Upload invalid file extension
    When I upload a file with invalid extension
    Then I should see the error "Please select a JSON file (.json extension)"
  Scenario: Upload and convert JSON file
    When I import the file "object-rooted.json"
    And I click the Convert button
    Then all column names should match the expected headers for "object-rooted"
    And each cell should match the expected table for "object-rooted"
