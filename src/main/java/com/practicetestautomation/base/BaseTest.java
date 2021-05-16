package com.practicetestautomation.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Properties;

import static com.practicetestautomation.base.BrowserDriverFactory.createDriver;

@Listeners({SauceLabsTestListener.class})
public class BaseTest {

    protected WebDriver driver;
    protected Logger log;

    @Parameters({"browser", "environment", "platform"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method,
            @Optional("chrome") String browser,
            @Optional("local") String environment,
            @Optional("WIN10") String platform,
            ITestContext ctx) {
        log = LogManager.getLogger(ctx.getCurrentXmlTest().getSuite().getName());
        setProperties();
        ctx.setAttribute("sauce", false);
        switch (environment) {
            case "local":
                driver = createDriver(browser, log);
                break;
            case "grid":
                driver = new GridFactory(browser, platform, log).createDriver();
                break;
            case "sauce":
                ctx.setAttribute("sauce", true);
                String sauceTestName = ctx.getName()
                        + " | " + method.getName()
                        + " | " + browser
                        + " | " + platform;
                SauceLabsFactory factory = new SauceLabsFactory(browser, platform, log, sauceTestName);
                driver = factory.createDriver();
                ctx.setAttribute("sessionId", factory.getSessionId());
                break;
            default:
                driver = createDriver(browser, log);
                break;
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    private void setProperties() {
        Properties props = System.getProperties();
        try {
            props.load(new FileInputStream(new File("src/main/resources/test.properties")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("Close driver");
        driver.quit();
    }
}
