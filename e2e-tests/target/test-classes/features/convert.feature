Feature: JSON convert to table
Background: I open the JSON viewer page

  @runonly
  Scenario: A correct table should be rendered when sample.json is converted
    When I click the Convert button
    Then the table headers should be:
      | id   | name  | value |
    Then the table rows should be:
      | 1  | 신길   | true  |
      | 2  | 홍길동 | false |
      | 3  | 김철수 | true  |

  @runonly
  Scenario: Convert other JSON files correctly
    When I import the file "arr_simple.json"
    And I click the Convert button
    Then all column names should match the keys in the JSON
    And each cell should match the corresponding JSON value