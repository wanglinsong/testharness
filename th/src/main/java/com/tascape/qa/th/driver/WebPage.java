package com.tascape.qa.th.driver;

import java.io.File;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class WebPage {
    private static final Logger LOG = LoggerFactory.getLogger(WebPage.class);

    protected WebBrowser webBrowser;

    private EntityDriver entityDriver;

    @CacheLookup
    @FindBy(tagName = "body")
    protected WebElement body;

    public EntityDriver getEntityDriver() {
        return entityDriver;
    }

    public void setWebBrowser(WebBrowser webBrowser) {
        this.webBrowser = webBrowser;
    }

    public void setEntityDriver(EntityDriver entityDriver) {
        this.entityDriver = entityDriver;
    }

    protected File captureScreen() {
        return this.entityDriver.captureScreen();
    }

    public void setSelect(WebElement select, String visibleText) {
        if (null == visibleText) {
            return;
        }
        Select s = new Select(select);
        if (visibleText.isEmpty()) {
            s.selectByIndex(1);
        } else {
            s.selectByVisibleText(visibleText);
        }
    }

    public void setSelect(By by, String visibleText) {
        WebElement select = this.webBrowser.findElement(by);
        this.setSelect(select, visibleText);
    }

    public String getSelect(By by) {
        WebElement select = this.webBrowser.findElement(by);
        Select s = new Select(select);
        return s.getFirstSelectedOption().getText();
    }

    public void highlight(WebElement element) {
        this.webBrowser.executeScript(Void.class, "arguments[0].style.border='3px solid red';", element);
    }
}