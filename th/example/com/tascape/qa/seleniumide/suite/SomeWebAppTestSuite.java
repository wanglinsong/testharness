package com.tascape.qa.seleniumide.suite;

import com.tascape.qa.seleniumide.driver.SomeWebApp;
import com.tascape.qa.seleniumide.test.SomeWebAppSeleniumIdeTests;
import com.tascape.qa.th.suite.SeleniumIdeSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SomeWebAppTestSuite extends SeleniumIdeSuite {
    private static final Logger LOG = LoggerFactory.getLogger(SomeWebAppTestSuite.class);

    @Override
    public void setUpTestClasses() {
        this.addTestClass(SomeWebAppSeleniumIdeTests.class);
    }

    @Override
    protected void setUpEnvironment() throws Exception {
        SomeWebApp app = new SomeWebApp();
        LOG.info("Check system property {}", SomeWebApp.SYSPROP_URL);
        app.setUrl(this.getSuiteProperty(SomeWebApp.SYSPROP_URL, "http://paypal.com"));

        this.putDirver(SomeWebAppSeleniumIdeTests.class, SomeWebAppSeleniumIdeTests.DRIVER_SOME_WEB_APP, app);
    }

    @Override
    protected void tearDownEnvironment() {
        // do nothing
    }
}