package runner;
import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "steps",
        // 태그 필터 없이 모든 시나리오를 돌린다. 일부만 돌리려면 -Dcucumber.filter.tags="..." 를 쓴다
        plugin = {"pretty",
                "summary",
                "html:target/cucumber-report.html",
                "json:target/cucumber-report.json",
                "junit:target/cucumber-report.xml"
        },
        monochrome = true
)
public class TestRunner {
}
