Feature: Export functionality
Background:
  Given I open the JSON viewer page

  Scenario: Export CSV file
    When I click the Convert button
    And I click the Export CSV button
    Then the downloaded CSV should be:
      | name | age | email | active | scores[0] | scores[1] | scores[2] | address.street | address.city | address.zip |
      | John Doe | 30 | john@example.com | true | 85 | 92 | 78 | 123 Main St | New York | 10001 |
      | Jane Smith | 25 | jane@example.com | false | 90 | 88 | 95 | 456 Oak Ave | Los Angeles | 90210 |
      | Bob Johnson | 35 | bob@example.com | true | 75 | 82 | 88 | 789 Pine Rd | Chicago | 60601 |

  # 쉼표·따옴표가 RFC 4180대로 감싸지지 않으면 파싱 결과의 열 개수와 값이 달라진다
  Scenario: CSV export quotes commas and double quotes
    When I enter JSON data:
      """
      [{"city": "Seoul, KR", "quote": "say \"hi\""}]
      """
    And I click the Convert button
    And I click the Export CSV button
    Then the downloaded CSV should be:
      | city      | quote    |
      | Seoul, KR | say "hi" |

  Scenario: Export CSV without data should show error
    When I click the Export CSV button
    Then I should see the error "No data to download. Please convert JSON first."

  Scenario: Export CSV after pivot
    When I click the Convert button
    And I click the Pivot button
    And I click the Export CSV button
    Then the downloaded CSV should be:
      | Field          | Row 1            | Row 2            | Row 3           |
      | name           | John Doe         | Jane Smith       | Bob Johnson     |
      | age            | 30               | 25               | 35              |
      | email          | john@example.com | jane@example.com | bob@example.com |
      | active         | true             | false            | true            |
      | scores[0]      | 85               | 90               | 75              |
      | scores[1]      | 92               | 88               | 82              |
      | scores[2]      | 78               | 95               | 88              |
      | address.street | 123 Main St      | 456 Oak Ave      | 789 Pine Rd     |
      | address.city   | New York         | Los Angeles      | Chicago         |
      | address.zip    | 10001            | 90210            | 60601           |
