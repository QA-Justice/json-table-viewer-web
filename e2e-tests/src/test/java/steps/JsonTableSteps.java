package steps;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import static org.junit.Assert.*;


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

    @When("I paste the following JSON:")
    public void pasteJson(String json) {
        WebElement input = driver.findElement(By.id("json-input"));
        input.clear();
        input.sendKeys(json);
    }

    @When("I click the Convert button")
    public void clickConvert() {
        WebElement button = driver.findElement(By.xpath("//button[contains(text(), 'Convert')]"));
        button.click();
    }


    @Then("I should see a table with {int} rows and {int} columns")
    public void checkTableSize(int rows, int columns) {
        WebElement table = driver.findElement(By.id("result-table"));
        int actualRows = table.findElements(By.cssSelector("tbody tr")).size();
        int actualCols = table.findElements(By.cssSelector("thead th")).size();
        assertEquals(rows, actualRows);
        assertEquals(columns, actualCols);
    }

    @Then("the column headers should include {string} and {string}")
    public void checkColumnHeaders(String col1, String col2) {
        WebElement table = driver.findElement(By.id("result-table"));
        String headers = table.findElement(By.tagName("thead")).getText();
        assertTrue(headers.contains(col1));
        assertTrue(headers.contains(col2));
    }
}
