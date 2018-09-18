/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.google.common.base.Function;
import com.thoughtworks.selenium.Wait;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 *
 * @author jasonlee
 */
public class ElementFinder {
    private WebDriver driver;

    public ElementFinder(WebDriver driver) {
        this.driver = driver;
    }

    public WebElement findElement(By locatorname, int timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(presenceOfElementLocated(locatorname));
    }

    public WebElement findElement(By locator, int timeout, ExpectedCondition<Boolean> condition) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        w.until(condition);

        return driver.findElement(locator);
    }

    private static Function<WebDriver, WebElement> presenceOfElementLocated(final By locator) {
        return new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                WebElement element = null;
                try {
                    element = driver.findElement(locator); 
                } catch (NoSuchElementException nse) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ElementFinder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    element = driver.findElement(locator); 
                }

                return element;
            }
        };
    }
}
