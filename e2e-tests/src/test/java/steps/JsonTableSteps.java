package steps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import locators.PageLocators ;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class JsonTableSteps {

    private static final String DEFAULT_BASE_URL = "https://qa-justice.github.io/json-table-viewer-web/";
    WebDriver driver;
    // CSV 다운로드 내용을 검증하려고 시나리오마다 빈 임시 폴더로 다운로드 위치를 지정한다
    Path downloadDir;

    @Before
    public void setup() throws IOException {
        downloadDir = Files.createTempDirectory("json-viewer-downloads");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toString());
        prefs.put("download.prompt_for_download", false);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @After //시나리오 종료 후 실행
    public void teardown() throws IOException {
        if (driver != null) {
            driver.quit(); //브라우저 세션 종료
        }
        if (downloadDir != null) {
            try (Stream<Path> paths = Files.walk(downloadDir)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.deleteIfExists(p);
                }
            }
        }
    }

    @Given("I open the JSON viewer page")
    public void openJsonViewer() {
        String baseUrl = System.getProperty("json.viewer.baseUrl");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = System.getenv("JSON_VIEWER_BASE_URL");
        }
        driver.get(baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl);

        // 앱 초기화(ES 모듈 로드 → 샘플 JSON 주입 → 이벤트 바인딩)가 끝날 때까지 기다린다.
        // 이걸 안 기다리면 업로드 주입이 바인딩보다 먼저 도착해서 change 이벤트가 버려진다.
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                d -> !d.findElement(By.xpath(PageLocators.JSON_INPUT)).getAttribute("value").trim().isEmpty());
    }


    @When("I click the Convert button")
    public void clickConvert() {
        WebElement button = driver.findElement(By.xpath(PageLocators.CONVERT_BUTTON));
        button.click();
    }

    @When("I click the Upload button")
    public void clickUpload() {
        WebElement button = driver.findElement(By.xpath(PageLocators.UPLOAD_BUTTON));
        button.click();
    }

    @When("I click the Pivot button")
    public void clickPivot() {
        WebElement button = driver.findElement(By.xpath(PageLocators.PIVOT_BUTTON));
        button.click();
    }


    @Then("the table headers should be:")
    public void theTableHeadersShouldBe(DataTable dataTable) {
        List<String> expectedHeaders = normalizeRow(dataTable.asLists().get(0));
        waitForElementVisible(PageLocators.TABLE_HEADER_TH);

        List<WebElement> headerElements = driver.findElements(By.xpath(PageLocators.TABLE_HEADER_TH));
        List<String> actualHeaders = headerElements.stream()
                .map(e -> e.getText().trim())
                .collect(Collectors.toList());

        assertEquals("Table headers do not match", expectedHeaders, actualHeaders);
    }

    @And("the table rows should be:")
    public void theTableRowsShouldBe(DataTable dataTable) {
        List<List<String>> expectedRows = dataTable.asLists().stream()
                .map(this::normalizeRow)
                .collect(Collectors.toList());

        waitForElementVisible(PageLocators.TABLE_BODY_ROWS);
        List<List<String>> actualRows = extractTableBodyRows();

        assertEquals("Table rows do not match", expectedRows, actualRows);
    }

    @When("I import the file {string}")
    public void iImportTheFile(String fileName) throws Exception {
        // 1. JSON 파일 절대경로 구하기 (src/test/resources/json/ 아래)
        Path path = Paths.get("src", "test", "resources", "json", fileName).toAbsolutePath();
        String absolutePath = path.toString();

        // 2. input[type=file] 요소 찾기 (display: none 이므로 JS로 일시적으로 보여줌)
        WebElement fileInput = driver.findElement(By.xpath(PageLocators.FILE_INPUT));

        if (!fileInput.isDisplayed()) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.display='block'; arguments[0].removeAttribute('hidden');", fileInput
            );
        }
        // 3. 실제 파일 경로를 input에 주입 (파일 선택한 것처럼 동작)
        fileInput.sendKeys(absolutePath);
    }


    @Then("I should see a success notification")
    public void iShouldSeeASuccessNotification() {
        // 성공 알림만 인정한다. 예전에는 성공 알림이 없으면 아무 notification 이나 통과시켰는데,
        // 에러 알림의 class 도 "notification notification-error" 라서 실패가 통과로 넘어갔다.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PageLocators.SUCCESS_NOTIFICATION)));
        } catch (TimeoutException e) {
            String actual = driver.findElements(By.xpath(PageLocators.NOTIFICATION)).stream()
                    .map(WebElement::getText)
                    .collect(Collectors.joining(" / "));
            fail("Success notification should be visible. Actual notification: "
                    + (actual.isEmpty() ? "(none)" : actual));
        }
    }

    @When("I upload a file with invalid extension")
    public void iUploadAFileWithInvalidExtension() {
        // 파일 업로드 버튼 클릭
        WebElement uploadButton = driver.findElement(By.xpath(PageLocators.UPLOAD_BUTTON));
        uploadButton.click();
        
        // 임시 텍스트 파일 생성 (잘못된 확장자)
        try {
            Path tempFile = Files.createTempFile("test", ".txt");
            Files.write(tempFile, "This is not a JSON file".getBytes());
            
            // 파일 업로드
            WebElement fileInput = driver.findElement(By.xpath(PageLocators.FILE_INPUT));
            fileInput.sendKeys(tempFile.toAbsolutePath().toString());
            
            // 임시 파일 삭제
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload invalid file", e);
        }
    }

    // 에러 알림이 "떴는지"만 보면 엉뚱한 이유의 에러도 통과하므로 메시지까지 확인한다
    @Then("I should see the error {string}")
    public void iShouldSeeTheError(String expectedMessage) {
        assertEquals("Error notification message should match", expectedMessage, errorNotificationText());
    }

    // 브라우저마다 JSON.parse 오류 문구가 달라서, 고정된 앞부분만 확인할 때 쓴다
    @Then("I should see an error containing {string}")
    public void iShouldSeeAnErrorContaining(String expectedPart) {
        String actual = errorNotificationText();
        assertTrue("Error notification should contain '" + expectedPart + "' but was: " + actual,
                actual.contains(expectedPart));
    }

    private String errorNotificationText() {
        try {
            waitForElementVisible(PageLocators.ERROR_NOTIFICATION);
        } catch (TimeoutException e) {
            String actual = driver.findElements(By.xpath(PageLocators.NOTIFICATION)).stream()
                    .map(WebElement::getText)
                    .collect(Collectors.joining(" / "));
            fail("Error notification should be visible. Actual notification: "
                    + (actual.isEmpty() ? "(none)" : actual));
        }
        return driver.findElement(By.xpath(PageLocators.ERROR_NOTIFICATION)).getText().trim();
    }

    @And("each cell should match the expected table for {string}")
    public void eachCellShouldMatchTheExpectedTableFor(String baseName) throws Exception {
        Path tablePath = Paths.get("src", "test", "resources", "expected", baseName + ".table.csv");

        // 기대값 파일: 첫 줄은 헤더, 나머지가 데이터 행이다.
        List<List<String>> expectedRows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(tablePath)) {
            boolean isHeaderRow = true;
            for (CSVRecord record : CSVFormat.DEFAULT.parse(reader)) {
                if (isHeaderRow) {
                    isHeaderRow = false;
                    continue;
                }
                List<String> row = new ArrayList<>();
                record.forEach(row::add);
                expectedRows.add(row);
            }
        }

        waitForElementVisible(PageLocators.TABLE_BODY_ROWS);
        List<List<String>> actualRows = extractTableBodyRows();

        assertEquals("Table rows do not match for: " + baseName,
                normalize(expectedRows), normalize(actualRows));
    }

    // JSON 데이터 입력 관련 step definitions
    @When("I enter JSON data:")
    public void i_enter_json_data(String docString) {
        WebElement jsonInput = driver.findElement(By.xpath(PageLocators.JSON_INPUT));
        jsonInput.clear();
        jsonInput.sendKeys(docString);
    }

    @When("I enter invalid JSON data")
    public void i_enter_invalid_json_data() {
        WebElement jsonInput = driver.findElement(By.xpath(PageLocators.JSON_INPUT));
        jsonInput.clear();
        jsonInput.sendKeys("{ invalid json data }");
    }

    @Then("the line-number gutter should show {string}")
    public void the_line_number_gutter_should_show(String expectedLines) {
        WebElement lineNumbers = driver.findElement(By.xpath(PageLocators.LINE_NUMBERS));
        String actualLines = lineNumbers.getAttribute("textContent").trim().replaceAll("\\s+", " ");
        assertEquals("Line-number gutter should match the JSON input", expectedLines, actualLines);
    }

    @Then("the JSON error position should be selected")
    public void the_json_error_position_should_be_selected() {
        WebElement jsonInput = driver.findElement(By.xpath(PageLocators.JSON_INPUT));
        WebElement activeElement = driver.switchTo().activeElement();
        assertEquals("JSON input should receive focus after a parse error", jsonInput, activeElement);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        long selectionStart = ((Number) js.executeScript("return arguments[0].selectionStart;", jsonInput)).longValue();
        long selectionEnd = ((Number) js.executeScript("return arguments[0].selectionEnd;", jsonInput)).longValue();
        assertTrue("The selected error position should not be the first character", selectionStart > 0);
        assertEquals("Exactly one invalid character should be selected", selectionStart + 1, selectionEnd);
    }

    @When("I clear the JSON input")
    public void i_clear_the_json_input() {
        WebElement jsonInput = driver.findElement(By.xpath(PageLocators.JSON_INPUT));
        jsonInput.clear();
    }

    // Copy 기능 관련 step definitions
    @When("I click the Copy button")
    public void i_click_the_copy_button() {
        WebElement copyButton = driver.findElement(By.xpath(PageLocators.COPY_BUTTON));
        copyButton.click();
    }

    @When("I select copy format {string}")
    public void i_select_copy_format(String format) {
        if ("markdown".equals(format)) {
            WebElement markdownOption = driver.findElement(By.xpath(PageLocators.COPY_MARKDOWN_OPTION));
            markdownOption.click();
        } else if ("text".equals(format)) {
            WebElement textOption = driver.findElement(By.xpath(PageLocators.COPY_TEXT_OPTION));
            textOption.click();
        }
    }

    // 성공 알림만으로는 복사된 내용이 맞는지 알 수 없어서, 클립보드를 실제로 읽어 비교한다
    @Then("the clipboard should contain:")
    public void the_clipboard_should_contain(String expected) {
        iShouldSeeASuccessNotification();
        assertEquals("Clipboard content should match", normalizeNewlines(expected), normalizeNewlines(readClipboard()));
    }

    @Then("the clipboard should contain tab-separated rows:")
    public void the_clipboard_should_contain_tab_separated_rows(DataTable dataTable) {
        String expected = dataTable.asLists().stream()
                .map(row -> String.join("\t", normalizeRow(row)))
                .collect(Collectors.joining("\n"));
        iShouldSeeASuccessNotification();
        assertEquals("Clipboard content should match", expected, normalizeNewlines(readClipboard()));
    }

    // 클립보드 API는 권한이 필요해서, 임시 textarea에 실제로 Ctrl+V 해서 읽는다 (사용자가 붙여넣는 것과 같은 경로)
    private String readClipboard() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement probe = (WebElement) js.executeScript(
                "const t = document.createElement('textarea');"
                        + "t.id = 'clipboard-probe';"
                        + "t.style.position = 'fixed'; t.style.top = '0'; t.style.left = '0';"
                        + "document.body.appendChild(t); t.focus(); return t;");
        probe.sendKeys(Keys.chord(Keys.CONTROL, "v"));
        String text = probe.getAttribute("value");
        js.executeScript("arguments[0].remove();", probe);
        return text;
    }

    private String normalizeNewlines(String text) {
        return text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    // Export 기능 관련 step definitions
    @When("I click the Export CSV button")
    public void i_click_the_export_csv_button() {
        WebElement exportButton = driver.findElement(By.xpath(PageLocators.EXPORT_BUTTON));
        exportButton.click();
    }

    // 다운로드된 CSV 파일을 열어 내용(첫 줄 헤더 포함)을 비교한다
    @Then("the downloaded CSV should be:")
    public void the_downloaded_csv_should_be(DataTable dataTable) throws IOException {
        Path csvFile = waitForDownloadedCsv();
        assertTrue("CSV file name should be json-table-YYYY-MM-DD.csv, was " + csvFile.getFileName(),
                csvFile.getFileName().toString().matches("json-table-\\d{4}-\\d{2}-\\d{2}\\.csv"));

        byte[] bytes = Files.readAllBytes(csvFile);
        assertTrue("CSV should start with a UTF-8 BOM so Excel reads it correctly",
                bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF);
        String content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);

        List<List<String>> actualRows = new ArrayList<>();
        for (CSVRecord record : CSVFormat.DEFAULT.parse(new StringReader(content))) {
            List<String> row = new ArrayList<>();
            record.forEach(row::add);
            actualRows.add(row);
        }

        List<List<String>> expectedRows = dataTable.asLists().stream()
                .map(this::normalizeRow)
                .collect(Collectors.toList());
        assertEquals("Downloaded CSV content should match", expectedRows, actualRows);
    }

    // 크롬은 .crdownload 로 받다가 완료되면 이름을 바꾸므로, .csv 가 생기면 다운로드가 끝난 것이다
    private Path waitForDownloadedCsv() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                try (Stream<Path> files = Files.list(downloadDir)) {
                    return files.filter(p -> p.getFileName().toString().endsWith(".csv")).findFirst().orElse(null);
                } catch (IOException e) {
                    return null;
                }
            });
        } catch (TimeoutException e) {
            fail("No CSV file was downloaded to " + downloadDir);
            return null;
        }
    }

    // Search 기능 관련 step definitions
    @When("I press Ctrl+F")
    public void i_press_ctrl_f() {
        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(Keys.chord(Keys.CONTROL, "f"));
    }

    @When("I type {string} in the search box")
    public void i_type_in_the_search_box(String text) {
        WebElement searchInput = driver.findElement(By.xpath(PageLocators.SEARCH_INPUT));
        searchInput.clear();
        searchInput.sendKeys(text);
    }

    @Then("the search box should be visible")
    public void the_search_box_should_be_visible() {
        waitForElementVisible(PageLocators.SEARCH_CONTAINER);
        WebElement searchContainer = driver.findElement(By.xpath(PageLocators.SEARCH_CONTAINER));
        assertTrue("Search box should be visible", searchContainer.isDisplayed());
    }

    @Then("the search box should not be visible")
    public void the_search_box_should_not_be_visible() {
        try {
            WebElement searchContainer = driver.findElement(By.xpath(PageLocators.SEARCH_CONTAINER));
            assertFalse("Search box should not be visible", searchContainer.isDisplayed());
        } catch (NoSuchElementException e) {
            // 요소가 없으면 보이지 않는 것으로 간주
        }
    }

    @Then("I should see {int} highlighted search results")
    public void i_should_see_highlighted_search_results(Integer expectedCount) {
        // 하이라이트 개수가 기대값이 될 때까지 기다린다.
        // 예전 구현은 size() >= 0 을 단언해서 검색이 통째로 죽어도 통과했다.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.numberOfElementsToBe(
                            By.xpath(PageLocators.SEARCH_HIGHLIGHT), expectedCount));
        } catch (TimeoutException e) {
            int actual = driver.findElements(By.xpath(PageLocators.SEARCH_HIGHLIGHT)).size();
            assertEquals("Number of highlighted search results should match",
                    expectedCount.intValue(), actual);
        }
    }

    @Then("I should see {int} current match highlights")
    public void i_should_see_current_match_highlights(Integer expectedCount) {
        List<WebElement> currentMatches = driver.findElements(By.xpath(PageLocators.CURRENT_MATCH));
        assertEquals("Number of current match highlights should match", expectedCount.intValue(), currentMatches.size());
    }

    @Then("the search count should show {string}")
    public void the_search_count_should_show(String expectedCount) {
        // "2/3" 을 통째로 비교한다. 예전에는 "/" 앞자리만 봐서 총 개수가 틀려도 통과했다.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.textToBe(By.xpath(PageLocators.SEARCH_COUNT), expectedCount));
        } catch (TimeoutException e) {
            String actual = driver.findElement(By.xpath(PageLocators.SEARCH_COUNT)).getText().trim();
            assertEquals("Search count should match", expectedCount, actual);
        }
    }

    @When("I click the search next button")
    public void i_click_the_search_next_button() {
        WebElement nextButton = driver.findElement(By.xpath(PageLocators.SEARCH_NEXT_BUTTON));
        nextButton.click();
    }

    @When("I click the search previous button")
    public void i_click_the_search_previous_button() {
        WebElement prevButton = driver.findElement(By.xpath(PageLocators.SEARCH_PREV_BUTTON));
        prevButton.click();
    }

    @When("I press Escape key")
    public void i_press_escape_key() {
        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(Keys.ESCAPE);
    }

    @When("I click the search close button")
    public void i_click_the_search_close_button() {
        WebElement closeButton = driver.findElement(By.xpath(PageLocators.SEARCH_CLOSE_BUTTON));
        closeButton.click();
    }

    @When("I press Enter in the search box")
    public void i_press_enter_in_the_search_box() {
        WebElement searchInput = driver.findElement(By.xpath(PageLocators.SEARCH_INPUT));
        searchInput.sendKeys(Keys.ENTER);
    }

    @When("I press Shift+Enter in the search box")
    public void i_press_shift_enter_in_the_search_box() {
        WebElement searchInput = driver.findElement(By.xpath(PageLocators.SEARCH_INPUT));
        searchInput.sendKeys(Keys.chord(Keys.SHIFT, Keys.ENTER));
    }

    // UI 상태 관련 step definitions
    @Then("I should see the no data message")
    public void i_should_see_the_no_data_message() {
        waitForElementVisible(PageLocators.NO_DATA_MESSAGE);
        WebElement noDataMessage = driver.findElement(By.xpath(PageLocators.NO_DATA_MESSAGE));
        assertTrue("No data message should be visible", noDataMessage.isDisplayed());
    }

    @Then("I should not see the no data message")
    public void i_should_not_see_the_no_data_message() {
        try {
            WebElement noDataMessage = driver.findElement(By.xpath(PageLocators.NO_DATA_MESSAGE));
            assertFalse("No data message should not be visible", noDataMessage.isDisplayed());
        } catch (NoSuchElementException e) {
            // 요소가 없으면 보이지 않는 것으로 간주
        }
    }

    @Then("the table should be empty")
    public void the_table_should_be_empty() {
        try {
            List<WebElement> rows = driver.findElements(By.xpath(PageLocators.TABLE_BODY_ROWS));
            assertTrue("Table should be empty", rows.isEmpty());
        } catch (Exception e) {
            // 테이블이 없거나 비어있으면 성공으로 간주
        }
    }

    @Then("the table should not be empty")
    public void the_table_should_not_be_empty() {
        waitForElementVisible(PageLocators.TABLE_BODY_ROWS);
        List<WebElement> rows = driver.findElements(By.xpath(PageLocators.TABLE_BODY_ROWS));
        assertTrue("Table should not be empty", !rows.isEmpty());
    }

    // Reset 기능 관련 step definitions
    @When("I click the Reset button")
    public void i_click_the_reset_button() {
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                d.findElements(By.xpath(PageLocators.NOTIFICATION)).stream()
                        .noneMatch(WebElement::isDisplayed));
        WebElement resetButton = driver.findElement(By.xpath(PageLocators.RESET_BUTTON));
        resetButton.click();
    }

    // Pivot 기능 관련 step definitions
    @Then("the Pivot button should show {string}")
    public void the_pivot_button_should_show(String expectedText) {
        WebElement pivotButton = driver.findElement(By.xpath(PageLocators.PIVOT_BUTTON));
        String actualText = pivotButton.getText();
        
        // 텍스트 정리: 특수문자 제거, 공백 정리
        actualText = actualText.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        actualText = actualText.replaceAll("\\s+", " ");
        
        // 디버깅을 위해 실제 값 출력
        System.out.println("Expected Pivot button text: '" + expectedText + "'");
        System.out.println("Actual Pivot button text: '" + pivotButton.getText() + "'");
        System.out.println("Cleaned actual text: '" + actualText + "'");
        
        assertEquals("Pivot button text should match", expectedText, actualText);
    }

    @Then("the table should be in pivot mode")
    public void the_table_should_be_in_pivot_mode() {
        // 앱은 <table> 이 아니라 #tableContainer 에 'pivoted' 클래스를 붙인다(table-renderer.js).
        // 예전 구현은 버튼 텍스트만 봐서, 버튼만 바뀌고 표는 그대로인 버그를 잡을 수 없었다.
        assertTrue("Table container should have the 'pivoted' class", isPivotedContainer());

        List<WebElement> headers = driver.findElements(By.xpath(PageLocators.TABLE_HEADER_TH));
        assertTrue("Pivoted table should have headers", !headers.isEmpty());
        assertEquals("First column of a pivoted table should be 'Field'",
                "Field", headers.get(0).getText().trim());
    }

    @Then("the table should not be in pivot mode")
    public void the_table_should_not_be_in_pivot_mode() {
        assertFalse("Table container should not have the 'pivoted' class", isPivotedContainer());
    }

    // 화면에서 보이도록 표 영역까지 내려간 뒤, 표 안을 부드럽게 끝까지 스크롤한다
    @When("I scroll to the bottom of the table")
    public void i_scroll_to_the_bottom_of_the_table() {
        WebElement container = driver.findElement(By.xpath(PageLocators.TABLE_CONTAINER));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'start', behavior: 'smooth'});", container);
        js.executeScript("arguments[0].scrollTo({top: arguments[0].scrollHeight, behavior: 'smooth'});", container);

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> (Boolean) js.executeScript(
                "const c = arguments[0]; return c.scrollTop > 0 && c.scrollTop + c.clientHeight >= c.scrollHeight - 1;",
                container));
    }

    @Then("the last table row should show {string}")
    public void the_last_table_row_should_show(String expectedFirstCell) {
        List<WebElement> rows = driver.findElements(By.xpath(PageLocators.TABLE_BODY_ROWS));
        assertFalse("Table should have rows", rows.isEmpty());
        WebElement lastRow = rows.get(rows.size() - 1);
        assertEquals("Last row's first cell should match", expectedFirstCell,
                lastRow.findElement(By.xpath("./td[1]")).getText().trim());

        WebElement container = driver.findElement(By.xpath(PageLocators.TABLE_CONTAINER));
        Boolean visible = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "const r = arguments[0].getBoundingClientRect(), c = arguments[1].getBoundingClientRect();"
                        + "return r.top >= c.top && r.bottom <= c.bottom + 1;",
                lastRow, container);
        assertTrue("Last row should be visible inside the table area after scrolling", visible);
    }

    // 고정(sticky) 헤더가 스크롤 후에도 표 영역의 맨 위에 붙어 있는지 위치로 확인한다
    @Then("the table header should stay at the top of the table")
    public void the_table_header_should_stay_at_the_top_of_the_table() {
        WebElement container = driver.findElement(By.xpath(PageLocators.TABLE_CONTAINER));
        WebElement firstHeader = driver.findElement(By.xpath(PageLocators.TABLE_HEADER_TH));
        Number gap = (Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].getBoundingClientRect().top - arguments[1].getBoundingClientRect().top;",
                firstHeader, container);
        assertTrue("Header should be stuck to the top of the table area, but was " + gap + "px below it",
                Math.abs(gap.doubleValue()) <= 2);
    }

    private boolean isPivotedContainer() {
        WebElement container = driver.findElement(By.xpath(PageLocators.TABLE_CONTAINER));
        String classAttribute = container.getAttribute("class");
        return classAttribute != null && classAttribute.contains("pivoted");
    }

    // 키보드 단축키 관련 step definitions
    @When("I press Ctrl+Enter in the JSON input")
    public void i_press_ctrl_enter_in_the_json_input() {
        WebElement jsonInput = driver.findElement(By.xpath(PageLocators.JSON_INPUT));
        jsonInput.sendKeys(Keys.chord(Keys.CONTROL, Keys.ENTER));
    }

    private List<String> normalizeRow(List<String> rawRow) {
        List<String> cleaned = new ArrayList<>();
        for (String cell : rawRow) {
            // Cucumber DataTable 은 빈 칸을 null 로 준다
            cleaned.add(cell == null ? "" : cell.trim().replaceAll("\\s+", " "));
        }
        return cleaned;
    }

    private List<List<String>> normalize(List<List<String>> rows) {
        return rows.stream().map(this::normalizeRow).collect(Collectors.toList());
    }

    @Then("all column names should match the expected headers for {string}")
    public void allColumnNamesShouldMatchTheExpectedHeadersFor(String baseName) throws Exception {
        // 1. 기대 헤더 파일 경로
        Path headersPath = Paths.get("src", "test", "resources", "expected", baseName + ".headers.txt");
        String expectedLine = Files.readString(headersPath).trim();

        // 2. '| id | name | age |' 같은 형식에서 셀 단위로 분리
        List<String> expectedHeaders = Arrays.stream(expectedLine.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 3. 실제 테이블 헤더 수집 (렌더링될 때까지 대기)
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PageLocators.TABLE_HEADER_TH)));
        List<WebElement> headerElements = driver.findElements(By.xpath(PageLocators.TABLE_HEADER_TH));
        List<String> actualHeaders = headerElements.stream()
                .map(e -> e.getText().trim())
                .collect(Collectors.toList());

        // 4. 비교
        assertEquals("Header does not match for file: " + baseName, expectedHeaders, actualHeaders);
    }

    private void waitForElementVisible(String xpathSelector) {
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathSelector)));
    }

    private List<List<String>> extractTableBodyRows() {
        List<WebElement> rowElements = driver.findElements(By.xpath(PageLocators.TABLE_BODY_ROWS));
        List<List<String>> actualRows = new ArrayList<>();

        for (WebElement row : rowElements) {
            List<String> rowData = row.findElements(By.xpath(".//td")).stream()
                    .map(cell -> cell.getText().trim())
                    .collect(Collectors.toList());
            actualRows.add(rowData);
        }
        return actualRows;
    }

}
