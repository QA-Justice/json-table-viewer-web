package steps;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import static org.junit.Assert.*;
import locators.PageLocators ;

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
    public void theTableHeadersShouldBe() {
        
    }

    @And("the table rows should be:")
    public void theFirstRowShouldBe() {
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
}
