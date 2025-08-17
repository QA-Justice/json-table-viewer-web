Feature: JSON convert to table
Background:
  Given I open the JSON viewer page

  @runonly
  Scenario: A correct table should be rendered when sample.json is converted
    When I click the Convert button
    Then the table headers should be:
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |
    And the table rows should be:
      | John Doe | 30 | john@example.com | true | 85 | 92 | 78 | 123 Main St | New York | 10001 |
      | Jane Smith | 25 | jane@example.com | false | 90 | 88 | 95 | 456 Oak Ave | Los Angeles | 90210 |
      | Bob Johnson | 35 | bob@example.com | true | 75 | 82 | 88 | 789 Pine Rd | Chicago | 60601 |

 #@runonly
#  Scenario: Convert other JSON files correctly
#    When I import the file "arr_simple.json"
#    And I click the Convert button
#    Then all column names should match the keys in the JSON
#    And each cell should match the corresponding JSON value