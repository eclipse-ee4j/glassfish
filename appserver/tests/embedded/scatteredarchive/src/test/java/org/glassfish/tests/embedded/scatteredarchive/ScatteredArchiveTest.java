/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.tests.embedded.scatteredarchive;

import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredEnterpriseArchive;
import org.glassfish.embeddable.web.HttpListener;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.tests.embedded.scatteredarchive.contextInitialized.ContextInitializedTestServlet;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.stream.Collectors.toList;
import static org.glassfish.tests.embedded.scatteredarchive.contextInitialized.ContextInitializedTestServlet.LABEL_CONTEXT_INITIALIZED_COUNTER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author bhavanishankar@dev.java.net
 */
public class ScatteredArchiveTest {

    private static final String PROJECT_DIR = System.getProperty("project.directory");
    System.Logger logger = System.getLogger(ScatteredArchiveTest.class.getName());

    GlassFish glassfish;
    GlassFishRuntime gfRuntime;

    @BeforeEach
    public void startGlassFish() throws GlassFishException {
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", 8080);
        gfRuntime = GlassFishRuntime.bootstrap();
        glassfish = gfRuntime.newGlassFish(props);
        glassfish.start();
    }

    @AfterEach
    public void stopGlassFish() throws GlassFishException {
        glassfish.dispose();
        gfRuntime.shutdown();
    }

    @Test
    public void testDefaults() throws Exception {

        Class<ScatteredArchiveTestServlet> servletClass = ScatteredArchiveTestServlet.class;
        String testServletName = servletClass.getSimpleName();

        // Test Scattered Web Archive
        ScatteredArchive sa = createDefaultArchive("scatteredarchive");
        URI warURI = sa.toURI();
        printContents(warURI);

        // Deploy archive
        Deployer deployer = glassfish.getDeployer();
        String appname = deployer.deploy(warURI);
        System.out.println("Deployed [" + appname + "]");
        Assertions.assertEquals(appname, "scatteredarchive");

        // Now create a http listener and access the app.
        WebContainer webcontainer = glassfish.getService(WebContainer.class);
        HttpListener listener = new HttpListener();
        listener.setId("my-listener");
        listener.setPort(9090);
        webcontainer.addWebListener(listener);

        get("http://localhost:9090/satest", "Hi, my name is Bhavani. What's yours?");
        get("http://localhost:9090/satest/" + testServletName,
                "Hi from " + testServletName);
        get("http://localhost:8080/satest/" + testServletName,
                "Hi from " + testServletName);

        deployer.undeploy(appname);

        // Test Scattered RA
        ScatteredArchive rar = new ScatteredArchive("scatteredra",
                ScatteredArchive.Type.RAR);
        rar.addClassPath(new File(PROJECT_DIR, "target/classes"));
        rar.addMetadata(new File(PROJECT_DIR, "src/main/config/ra.xml"));
        URI rarURI = rar.toURI();
        printContents(rarURI);
        appname = deployer.deploy(rarURI);
        System.out.println("Deployed RAR [" + appname + "]");
        Assertions.assertEquals(appname, "scatteredra");

        // Test Scattered Enterprise Archive.
        ScatteredEnterpriseArchive ear = new ScatteredEnterpriseArchive("sear");
        ear.addArchive(warURI, "sa.war");
        ear.addArchive(rarURI);
        ear.addMetadata(new File(PROJECT_DIR, "src/main/config/application.xml"));
        URI earURI = ear.toURI();
        printContents(earURI);
        appname = deployer.deploy(earURI);
        System.out.println("Deployed [" + appname + "]");
        Assertions.assertEquals(appname, "sear");

        get("http://localhost:9090/satest", "Hi, my name is Bhavani. What's yours?");
        get("http://localhost:9090/satest/" + testServletName,
                "Hi from " + testServletName);
        get("http://localhost:8080/satest/" + testServletName,
                "Hi from " + testServletName);

    }

    @Test
    public void testContextInitialized() throws Exception {
        Class<ContextInitializedTestServlet> servletClass = ContextInitializedTestServlet.class;
        String ARCHIVE_NAME = servletClass.getSimpleName() + "Archive";

        ScatteredArchive sa = createDefaultArchive(ARCHIVE_NAME);
        String testClassesSubPath = servletClass.getPackageName().replace('.', File.separatorChar);
        String additionalClassPath = "target/" + servletClass.getSimpleName() + "-classes/";

        // copy test-specific classes and add them to the archive
        FileUtils.copyTree(new File(PROJECT_DIR, "target/test-classes/" + testClassesSubPath),
                new File(PROJECT_DIR, additionalClassPath + testClassesSubPath));
        sa.addClassPath(new File(PROJECT_DIR, additionalClassPath));

        // add some JAR files to the archive
        sa.addClassPath(new File(PROJECT_DIR, "target/test-dependencies/hamcrest.jar"));
        sa.addClassPath(new File(PROJECT_DIR, "target/test-dependencies/junit-jupiter-engine.jar"));

        URI warURI = sa.toURI();
        printContents(warURI);

        // Deploy archive
        Deployer deployer = glassfish.getDeployer();
        String appname = deployer.deploy(warURI);
        logger.log(INFO, "Deployed [" + appname + "]");
        Assertions.assertEquals(appname, ARCHIVE_NAME);

        get("http://localhost:8080/satest/" + ContextInitializedTestServlet.class.getSimpleName(),
                LABEL_CONTEXT_INITIALIZED_COUNTER, "1");
    }

    private ScatteredArchive createDefaultArchive(String ARCHIVE_NAME) throws IOException {
        // Test Scattered Web Archive
        ScatteredArchive sa = new ScatteredArchive(ARCHIVE_NAME,
                ScatteredArchive.Type.WAR, new File(PROJECT_DIR, "src/main/webapp"));
        sa.addClassPath(new File(PROJECT_DIR, "target/classes"));
        sa.addClassPath(new File(PROJECT_DIR, "src/main/resources"));
        return sa;
    }

    private void get(String urlStr, String containingString) throws Exception {
        List<String> inLines = getLinesFromUrl(new URL(urlStr));
        MatcherAssert.assertThat("Output from servlet", inLines, hasItem(containsString(containingString)));
        logger.log(INFO, "***** SUCCESS **** Found [" + containingString + "] in the response.*****");
    }

    private void get(String urlStr, String key, String value) throws Exception {
        List<String> inLines = getLinesFromUrl(new URL(urlStr));
        String result = key + ":" + value;
        MatcherAssert.assertThat("Output from servlet", inLines, hasItem(is(result)));
        logger.log(INFO, "***** SUCCESS **** Found [" + result + "] in the response.*****");
    }

    private List<String> getLinesFromUrl(URL url) throws Exception {
        URLConnection yc = url.openConnection();
        logger.log(DEBUG, "\nURLConnection [" + yc + "] : ");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()))) {
            return in.lines().collect(toList());
        }
    }

    void printContents(URI jarURI) throws IOException {
        JarFile jarfile = new JarFile(new File(jarURI));
        StringBuilder contents = new StringBuilder();
        contents.append("[")
                .append(jarURI)
                .append("] contents : \n");
        Enumeration<JarEntry> entries = jarfile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            contents.append(entry.getSize())
                    .append("\t")
                    .append(new Date(entry.getTime()))
                    .append("\t")
                    .append(entry.getName())
                    .append("\n");
        }
        logger.log(INFO, contents);
    }

}
