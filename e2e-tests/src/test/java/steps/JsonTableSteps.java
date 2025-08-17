package steps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import locators.PageLocators ;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class JsonTableSteps {

    WebDriver driver;

    @Before
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @After //시나리오 종료 후 실행
    public void teardown() {
        if (driver != null) {
            driver.quit(); //브라우저 세션 종료
        }
    }

    @Given("I open the JSON viewer page")
    public void openJsonViewer() {
        driver.get("http://localhost:8080/index-improved.html");
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

        List<WebElement> headerElements = driver.findElements(By.cssSelector(PageLocators.TABLE_HEADER_TH));
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


    @Then("all column names should match the keys in the JSON")
    public void allColumnNamesShouldMatchTheKeysInTheJSON() {
    }

    @And("each cell should match the expected table for {string}")
    public void eachCellShouldMatchTheExpectedTableFor(String baseName) throws Exception {
        Path tablePath = Paths.get("src", "test", "resources", "expected", baseName + ".table.csv");
        List<List<String>> expectedRows = Files.readAllLines(tablePath).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> Arrays.stream(line.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        waitForElementVisible(PageLocators.TABLE_BODY_ROWS);
        List<List<String>> actualRows = extractTableBodyRows();

        assertEquals("Table rows do not match for: " + baseName, expectedRows, actualRows);
    }

    private List<String> normalizeRow(List<String> rawRow) {
        List<String> cleaned = new ArrayList<>();
        for (String cell : rawRow) {
            cleaned.add(cell.trim().replaceAll("\\s+", " "));
        }
        return cleaned;
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
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(PageLocators.TABLE_HEADER_TH)));
        List<WebElement> headerElements = driver.findElements(By.cssSelector(PageLocators.TABLE_HEADER_TH));
        List<String> actualHeaders = headerElements.stream()
                .map(e -> e.getText().trim())
                .collect(Collectors.toList());

        // 4. 비교
        assertEquals("Header does not match for file: " + baseName, expectedHeaders, actualHeaders);
    }

    private void waitForElementVisible(String cssSelector) {
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
    }

    private List<List<String>> extractTableBodyRows() {
        List<WebElement> rowElements = driver.findElements(By.cssSelector(PageLocators.TABLE_BODY_ROWS));
        List<List<String>> actualRows = new ArrayList<>();

        for (WebElement row : rowElements) {
            List<String> rowData = row.findElements(By.cssSelector("td")).stream()
                    .map(cell -> cell.getText().trim())
                    .collect(Collectors.toList());
            actualRows.add(rowData);
        }
        return actualRows;
    }

}
