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

import com.thoughtworks.selenium.Selenium;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.admingui.devtests.BaseSeleniumTestClass;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 *
 * @author jasonlee
 */
public class SeleniumHelper {
    private static SeleniumHelper instance;
    private SeleniumWrapper selenium;
    private WebDriver driver;
    private ElementFinder elementFinder;
    private static final Logger logger = Logger.getLogger(SeleniumHelper.class.getName());

    private SeleniumHelper() {
    }

    public synchronized static SeleniumHelper getInstance() {
        if (instance == null) {
            instance = new SeleniumHelper();
        }

        return instance;
    }

    public SeleniumWrapper getSeleniumInstance() {
        if (selenium == null) {
            if (Boolean.parseBoolean(SeleniumHelper.getParameter("debug", "false"))) {
                logger.log(Level.INFO, "Creating new selenium instance");
            }
            String browser = getParameter("browser", "firefox");

            if ("firefox".equals(browser)) {
                driver = new FirefoxDriver();
            } else if ("chrome".equals(browser)) {
                driver = new ChromeDriver();
            } else if ("ie".contains(browser)) {
                driver = new InternetExplorerDriver();
            }
            elementFinder = new ElementFinder(driver);

            selenium = new SeleniumWrapper(driver, getBaseUrl());
            selenium.setTimeout("90000");
            (new BaseSeleniumTestClass()).openAndWaitForHomePage(getBaseUrl(), BaseSeleniumTestClass.TRIGGER_COMMON_TASKS, 480); // Make sure the server has started and the user logged in
        }

        selenium.windowFocus();
        selenium.windowMaximize();
        selenium.setTimeout("90000");

        return selenium;
    }

    public void releaseSeleniumInstance() {
        if (selenium != null) {
            if (Boolean.parseBoolean(SeleniumHelper.getParameter("debug", "false"))) {
                logger.log(Level.INFO, "Releasing selenium instance");
            }
            selenium.stop();
            selenium = null;
        }
    }

    public String getBaseUrl() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(SeleniumHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "http://" + hostName + ":" + getParameter("admin.port", "4848");
    }

    public WebDriver getDriver() {
        return driver;
    }

    public ElementFinder getElementFinder() {
        return elementFinder;
    }


    public static String getParameter(String paramName, String defaultValue) {
        String value = System.getProperty(paramName);

        return value != null ? value : defaultValue;
    }

    public static String captureScreenshot() {
        return captureScreenshot("" + (Math.abs(new Random().nextInt()) + 1));
    }

    public static String captureScreenshot(String fileName) {
        try {
            new File("target/surefire-reports/").mkdirs(); // Insure directory is there
            FileOutputStream out = new FileOutputStream("target/surefire-reports/screenshot-" + fileName + ".png");
            out.write(((TakesScreenshot)getInstance().getDriver()).getScreenshotAs(OutputType.BYTES));
            out.close();
        } catch (Exception e) {
            // No need to crash the tests if the screenshot fails
        }

        return fileName;
    }
}
