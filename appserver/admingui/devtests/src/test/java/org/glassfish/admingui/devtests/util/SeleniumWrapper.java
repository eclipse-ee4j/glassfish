/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admingui.devtests.util;

import org.glassfish.admingui.devtests.BaseSeleniumTestClass;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

/**
 *
 * @author jasonlee
 */
public class SeleniumWrapper extends WebDriverBackedSelenium {

    WebDriver driver;

    public SeleniumWrapper(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.driver = driver;
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public String getValue(String elem) {
        String value = null;
        try {
            value = super.getValue(elem);
        } catch (Exception e) {
            sleep(1000);
            value = super.getValue(elem);
        }
        return value;
    }

    @Override
    public void click(String locator) {
        try {
            super.click(locator);
        } catch (Exception e) {
            sleep(1000);
            super.click(locator);
        }
    }

    @Override
    public String getText(String locator) {
        String text = null;
        try {
            text = super.getText(locator);
        } catch (Exception e) {
            sleep(1000);
            text = super.getText(locator);
        }
        return text;
    }

    @Override
    public void type(String locator, String value) {
        try {
            super.type(locator, value);
        } catch (Exception e) {
            sleep(1000);
            super.type(locator, value);
        }
    }

    @Override
    public void uncheck(String string) {
        try {
            super.uncheck(string);
        } catch (Exception e) {
            sleep(1000);
            super.uncheck(string);
        }
    }

    @Override
    public void check(String string) {
        try {
            super.check(string);
        } catch (Exception e) {
            sleep(1000);
            super.check(string);
        }
    }

    @Override
    public void select(String string, String string1) {
        try {
            super.select(string, string1);
        } catch (Exception e) {
            sleep(1000);
            super.select(string, string1);
        }
    }

    @Override
    public void removeAllSelections(String string) {
        try {
            super.removeAllSelections(string);
        } catch (Exception e) {
            sleep(1000);
            super.removeAllSelections(string);
        }
    }

    @Override
    public String getAttribute(String string) {
        String attribute = null;
        try {
            attribute = super.getAttribute(string);
        } catch (Exception e) {
            sleep(1000);
            attribute = super.getAttribute(string);
        }
        return attribute;
    }

    @Override
    public boolean isTextPresent(String string) {
        boolean isTextPresent = false;
        try {
            isTextPresent = super.isTextPresent(string);
        } catch (Exception e) {
            sleep(1000);
            isTextPresent = super.isTextPresent(string);
        }
        return isTextPresent;
    }

    @Override
    public void waitForCondition(String string, String string1) {
        try {
            super.waitForCondition(string, string1);
        } catch (Exception e) {
            sleep(1000);
            super.waitForCondition(string, string1);
        }
    }

    @Override
    public String getEval(String string) {
        String eval = null;
        try {
            eval = super.getEval(string);
        } catch (Exception e) {
            sleep(1000);
            eval = super.getEval(string);
        }
        return eval;
    }

    @Override
    public String[] getSelectOptions(String string) {
        String options[] = null;
        try {
            options = super.getSelectOptions(string);
        } catch (Exception e) {
            sleep(1000);
            options = super.getSelectOptions(string);
        }
        return options;
    }

    @Override
    public boolean isChecked(String string) {
        boolean isChecked = false;
        try {
            isChecked = super.isChecked(string);
        } catch (Exception e) {
            sleep(1000);
            isChecked = super.isChecked(string);
        }
        return isChecked;
    }

    @Override
    public void addSelection(String string, String string1) {
        try {
            super.addSelection(string, string1);
        } catch (Exception e) {
            sleep(1000);
            super.addSelection(string, string1);
        }
    }

    @Override
    public void submit(String string) {
        try {
            super.submit(string);
        } catch (Exception e) {
            sleep(1000);
            super.submit(string);
        }
    }

    @Override
    public void waitForPopUp(String string, String string1) {
        try {
            super.waitForPopUp(string, string1);
        } catch (Exception e) {
            sleep(1000);
            super.waitForPopUp(string, string1);
        }
    }

    @Override
    public void chooseOkOnNextConfirmation() {
        try {
            super.chooseOkOnNextConfirmation();
        } catch (Exception e) {
            sleep(1000);
            super.chooseOkOnNextConfirmation();
        }
    }

    @Override
    public boolean isConfirmationPresent() {
        boolean confirmationPresent = false;
        try {
            confirmationPresent = super.isConfirmationPresent();
        } catch (Exception e) {
            sleep(1000);
            confirmationPresent = super.isConfirmationPresent();
        }
        return confirmationPresent;
    }

    @Override
    public boolean isAlertPresent() {
        boolean alertPresent = false;
        try {
            alertPresent = super.isAlertPresent();
        } catch (Exception e) {
            sleep(1000);
            alertPresent = super.isAlertPresent();
        }
        return alertPresent;
    }

    @Override
    public String getConfirmation() {
        String confirmation = null;

        try {
            confirmation = super.getConfirmation();
        } catch (Exception e) {
            sleep(1000);
            confirmation = super.getConfirmation();
        }
        return confirmation;
    }

    @Override
    public void attachFile(String string, String string1) {
        try {
            super.attachFile(string, string1);
        } catch (Exception e) {
            sleep(1000);
            super.attachFile(string, string1);
        }
    }

    public WebElement findElement(By by) {
        WebElement element = (WebElement) driver.findElement(by);

        try {
            element.isDisplayed(); // Force an op on the element to make sure it's a valid reference
        } catch (Exception e) {
            sleep(1000);
            element = (WebElement) driver.findElement(by);
        }

        return element;
    }

    public WebElement findElement(By by, int timeoutInSeconds) {
        for (int seconds = 0;; seconds++) {
            if (seconds >= (timeoutInSeconds)) {
                Assert.fail("The operation timed out waiting for the page to load.");
            }

            WebElement element = null;

            try {
                element = (WebElement) driver.findElement(by);
                element.isDisplayed();
                return element;
            } catch (Exception ex) {
            }

            sleep(BaseSeleniumTestClass.TIMEOUT_CALLBACK_LOOP);
        }
    }
}
