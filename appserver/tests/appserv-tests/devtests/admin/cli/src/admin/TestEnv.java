/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * TestEnv -- static methods for figuring out directories, files, etc.
 *
 *
 * @author Byron Nevins
 */
package admin;

import java.io.File;

public final class TestEnv {
    public static boolean isHadas() {
        return isHadas;
    }
    public static boolean isV3Layout() {
        return !isHadas();
    }
    public static boolean isV4Layout() {
        return isHadas();
    }
    public static File getGlassFishHome() {
        return gf_home;
    }
    public static File getDomainsHome() {
        return domains_home;
    }
    public static File getDomainHome(String domainName) {
        return new File(getDomainsHome(), domainName);
    }
    public static File getDomainServerHome(String domainName) {
        if(isHadas())
            return new File(getDomainHome(domainName), "server");
        else
            return getDomainHome(domainName);
    }
    public static File getDomainDocRoot(String domainName) {
        return new File(getDomainServerHome(domainName), DOCROOT);
    }
    public static File getDomainConfigDir(String domainName) {
        return new File(getDomainServerHome(domainName), CONFIG);
    }
    public static File getInfoDir(String domainName) {
        return new File(getDomainServerHome(domainName), INFO_DIRECTORY);
    }
    public static File getDomainXml(String domainName) {
        return new File(getDomainConfigDir(domainName), DOMAIN_XML);
    }
    public static File getConfigSpecificConfigDir(String domainName, String instanceName) {
        return new File(getDomainConfigDir(domainName), instanceName + "-config");
    }
    public static File getConfigSpecificDocRoot(String domainName, String instanceName) {
        return new File(getConfigSpecificConfigDir(domainName, instanceName), DOCROOT);
    }
    public static File getNodesHome() {
        if(isHadas())
            return getDomainsHome();
        else
            return new File(getGlassFishHome(), "nodes");
    }
    public static File getDasPropertiesFile(String nodeName) {
        // it goes in one of these (both are the DEFAULT locations for illustration)
        if (isHadas())
            return new File(TestEnv.getDomainHome(), DAS_PROPS_PATH);
        else
            return new File(TestEnv.getInstancesHome(nodeName), DAS_PROPS_PATH);
    }

    public static File getInstancesHome(String domainName, String nodeName) {
        if(isHadas())
            return getDomainHome(domainName);
        else
            return new File(getNodesHome(), nodeName);
    }
    public static File getInstanceDir(String domainName, String nodeName, String instanceName) {
        return new File(getInstancesHome(domainName, nodeName), instanceName);
    }
    public static File getInstanceConfigDir(String domainName, String nodeName, String instanceName) {
        return new File(getInstanceDir(domainName, nodeName, instanceName), CONFIG);
    }
    public static File getInstanceDomainXml(String domainName, String nodeName, String instanceName) {
        return new File(getInstanceConfigDir(domainName, nodeName, instanceName), DOMAIN_XML);
    }
    public static File getDomainLog(String domainName) {
        return new File(getDomainServerHome(domainName), SERVER_LOG);
    }
    public static File getDomainInfoXml(String domainName) {
        return new File(getInfoDir(domainName), DOMAIN_INFO_XML);
    }
    // ***************************
    // convenience methods that plug-in "domain1"
    // ****************************/

    public static File getDomainHome() {
        return getDomainHome(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainServerHome() {
        return getDomainServerHome(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainDocRoot() {
        return getDomainDocRoot(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainConfigDir() {
        return getDomainConfigDir(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainXml() {
        return getDomainXml(DEFAULT_DOMAIN_NAME);
    }
    public static File getConfigSpecificConfigDir(String instanceName) {
        return getConfigSpecificConfigDir(DEFAULT_DOMAIN_NAME, instanceName);
    }
    public static File getConfigSpecificDocRoot(String instanceName) {
        return getConfigSpecificDocRoot(DEFAULT_DOMAIN_NAME, instanceName);
    }
    public static File getInstancesHome(String nodeName) {
        return getInstancesHome(DEFAULT_DOMAIN_NAME, nodeName);
    }
    public static File getInstanceDir(String nodeName, String instanceName) {
        return getInstanceDir(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getInstanceConfigDir(String nodeName, String instanceName) {
        return getInstanceConfigDir(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getInstanceDomainXml(String nodeName, String instanceName) {
        return getInstanceDomainXml(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getDomainLog() {
        return new File(getDomainServerHome(), SERVER_LOG);
    }
    public static File getDefaultTemplateDir() {
        return new File(gf_home, DEFUALT_TEMPLATE_RELATIVE_PATH);
    }

    ///////////////////////////////////////////////////////////////////////
    //  internal stuff below
    //////////////////////////////////////////////////////////////////////
    private TestEnv() {
        // no instances allowed!
    }

    private static final boolean isHadas;
    private static final File gf_home;
    private static final File domains_home;
    private static final String DEFAULT_DOMAIN_NAME = "domain1";
    private static final String DOCROOT = "docroot";
    private static final String CONFIG = "config";
    private static String DOMAIN_XML = "domain.xml";
    private static final String DAS_PROPS_PATH = "agent/config/das.properties";
    private final static String SERVER_LOG = "logs/server.log";

    /** Name of directory stores the domain information. */
    private static final String INFO_DIRECTORY = "init-info";
    /** The file name stores the basic domain information. */
    private static final String DOMAIN_INFO_XML = "domain-info.xml";
    private final static String DEFUALT_TEMPLATE_RELATIVE_PATH = "common" + File.separator + "templates" + File.separator + "gf";

    static {
        isHadas = Boolean.getBoolean("HADAS")
                || Boolean.getBoolean("hadas")
                || Boolean.parseBoolean(System.getenv("hadas"))
                || Boolean.parseBoolean(System.getenv("HADAS"));

        File gf_homeNotFinal = null;

        try {
            String home = System.getenv("S1AS_HOME");

            if (home == null) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("No S1AS_HOME set!");
            }

            gf_homeNotFinal = new File(home);

            try {
                gf_homeNotFinal = gf_homeNotFinal.getCanonicalFile();
            }
            catch (Exception e) {
                gf_homeNotFinal = gf_homeNotFinal.getAbsoluteFile();
            }

            if (!gf_homeNotFinal.isDirectory()) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("S1AS_HOME is not pointing at a real directory!");
            }
        }
        catch(IllegalStateException e) {
            // what's the point of struggling on?
            System.out.println("#####  CATASTROPHIC ERROR -- You must set S1AS_HOME to point to the GlassFish installation directory");
            System.exit(2);
        }
        finally {
            gf_home = gf_homeNotFinal;
            domains_home = new File(gf_home, "domains");
        }
    }
}
