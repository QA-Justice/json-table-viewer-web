package steps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        // Feature 파일에서 가져온 기대 헤더
        List<String> expectedHeaders = normalizeRow(dataTable.asLists().get(0));
        // 테이블이 로딩될 때까지 최대 5초 대기
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#json-table thead th")));
        // 실제 테이블 헤더 가져오기
        List<WebElement> headerElements = driver.findElements(By.cssSelector("#json-table thead th"));
        List<String> actualHeaders = new ArrayList<>();
        for (WebElement header : headerElements) {
            actualHeaders.add(header.getText().trim());
        }
        // 기대값과 실제값 비교
        assertEquals("Table headers do not match", expectedHeaders, actualHeaders);
    }


    @And("the table rows should be:")
    public void theTableRowsShouldBe(DataTable dataTable) {
        // Feature 파일에서 입력한 기대값 정제
        List<List<String>> expectedRows = dataTable.asLists();
        List<List<String>> normalizedExpectedRows = new ArrayList<>();
        for (List<String> row : expectedRows) {
            normalizedExpectedRows.add(normalizeRow(row));
        }
        // 실제 테이블의 <tbody> 안 <tr> 가져오기
        List<WebElement> rowElements = driver.findElements(By.cssSelector("#json-table tbody tr"));
        List<List<String>> actualRows = new ArrayList<>();
        for (WebElement row : rowElements) {
            // 각 행 안의 <td> 셀 가져오기
            List<WebElement> cellElements = row.findElements(By.cssSelector("td"));
            List<String> rowData = new ArrayList<>();
            for (WebElement cell : cellElements) {
                rowData.add(cell.getText().trim());
            }
            actualRows.add(rowData);
        }
        // 기대값과 실제 테이블 데이터 비교
        assertEquals("Table rows do not match", normalizedExpectedRows, actualRows);
    }

    @When("I import the file {string}")
    public void iImportTheFile(String arg0) {
    }

    @Then("all column names should match the keys in the JSON")
    public void allColumnNamesShouldMatchTheKeysInTheJSON() {
    }

    @And("each cell should match the corresponding JSON value")
    public void eachCellShouldMatchTheCorrespondingJSONValue() {
    }


    private List<String> normalizeRow(List<String> rawRow) {
        List<String> cleaned = new ArrayList<>();
        for (String cell : rawRow) {
            cleaned.add(cell.trim().replaceAll("\\s+", " "));
        }
        return cleaned;
    }
}
