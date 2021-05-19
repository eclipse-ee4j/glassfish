/*
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

package admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;

/**
 *
 * @author tmueller
 */
public class DomainTest extends AdminBaseDevTest {

    private final String NUCLEUS_DOMAIN_TEMPLATE_NAME = "nucleus-domain.jar";
    private final String DEFAULT_DOMAIN_TEMPLATE_NAME = "default_domain_template";
    private final String BRANDING_FILE_RELATIVE_PATH = "config" + File.separator + "branding" + File.separator + "glassfish-version.properties";
    private Properties _brandingProperties;

    public DomainTest(){
        init();
    }

    private void init() {
        if (_brandingProperties == null) {
            _brandingProperties = new Properties();
            try {
                File brandingFile = new File(TestEnv.getGlassFishHome(), BRANDING_FILE_RELATIVE_PATH);
                _brandingProperties.load(new FileInputStream(brandingFile));
            } catch (IOException e) {
                System.out.println("Not able to load branding file.");
            }
        }
    }

    private String getDefaultTemplateName() {
        return _brandingProperties != null ? _brandingProperties.getProperty(DEFAULT_DOMAIN_TEMPLATE_NAME,
                NUCLEUS_DOMAIN_TEMPLATE_NAME) : NUCLEUS_DOMAIN_TEMPLATE_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Tests domain functionality such as create-domain, etc.";
    }

    public static void main(String[] args) {
        new DomainTest().runTests();
    }

    private void runTests() {
        testCreateDomain();
        testDeleteDomain();
        stat.printSummary();
    }

    /**
     * Test domain creation.
     */
    void testCreateDomain() {
        final String tn = "create-domain-template-";
        File defaultDomainDir = TestEnv.getDefaultTemplateDir();

        // Test domain creation for the default template.
        report(tn + "create-domain1", asadmin("create-domain",
                "--nopassword=true", "--checkports=false", "domt1"));
        report(tn + "check1", checkDomain("domt1", new File(defaultDomainDir, getDefaultTemplateName()).getAbsolutePath()));
        report(tn + "delete-domain1", asadmin("delete-domain", "domt1"));

        File templateJar = new File(TestEnv.getDefaultTemplateDir(), NUCLEUS_DOMAIN_TEMPLATE_NAME);
        // Test domain creation with --template argument.
        if (templateJar.exists()) {
            report(tn + "create-domain2", asadmin("create-domain",
                    "--nopassword=true", "--checkports=false", "--template",
                    templateJar.getAbsolutePath(), "domt2"));
            report(tn + "check2", checkDomain("domt2", templateJar.getAbsolutePath()));
            report(tn + "delete-domain2", asadmin("delete-domain", "domt2"));
        }
    }

    /**
     * Check's the template used to create domain against the given template path.
     *
     * @param name Domain name.
     * @param templatePath absolute template path.
     * @return true if the given template is used for domain creation.
     */
    boolean checkDomain(String name, String templatePath) {
        File domInfoXml = TestEnv.getDomainInfoXml(name);
        String xpathExpr = "//@location";
        Object o = evalXPath(xpathExpr, domInfoXml, XPathConstants.STRING);
        if (!(o instanceof String && templatePath.equals((String)o))) {
            return false;
        }
        File domainFile = getDASDomainXML(name);
        return domainFile.exists();
    }

    void testDeleteDomain() {
        final String tn = "delete-domain-";
        report(tn + "create", asadmin("create-domain", "foo"));
        report(tn + "baddir", !asadmin("delete-domain", "--domainsdir", "blah", "foo"));
        report(tn + "delete", asadmin("delete-domain", "foo"));
    }
}
