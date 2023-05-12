import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumTest {

    public static void main(String[] args) {

        SeleniumTest selTest = new SeleniumTest();
        selTest.crawl();

    }


    //WebDriver
    private final WebDriver driver;

    //Properties
    public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static final String WEB_DRIVER_PATH = "src/chromedriver";

    public SeleniumTest() {
        super();

        //System Property SetUp
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);


        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        //Driver SetUp
        driver = new ChromeDriver(options);
    }

    public void crawl() {

        try {
            //get page (= 브라우저에서 url을 주소창에 넣은 후 request 한 것과 같다)
            //크롤링 할 URL
            String base_url = "https://www.naver.com";
            driver.get(base_url);
            System.out.println(driver.getPageSource());

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            driver.close();
        }

    }

}
