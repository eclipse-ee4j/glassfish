/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier;

import java.io.File;
import java.util.List;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.tools.verifier.util.VerifierConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.deployment.archive.Archive;

/**
 * This is a data class that contains all the argument specific stuff and
 * the temporary variables. It is used during the verification process and
 * during the report generation.
 *
 * @author Vikas Awasthi
 */
public class VerifierFrameworkContext
{

    private boolean app = false;
    private boolean appClient = false;
    private boolean connector = false;
    private boolean ejb = false;
    private boolean web = false;
    private boolean webServices = false;
    private boolean webServicesClient = false;
    private boolean persistenceUnits = false; // EJB 3.0 persistence entity

    private boolean partition = false;
    private int reportLevel = VerifierConstants.WARN;
    private boolean useTimeStamp = false;
    private boolean usingGui = false;
    private boolean isBackend = false;
    private String jarFileName = null;
    private String outputDirName = null;
    private String explodedArchivePath = null;
    private ResultManager resultManager = new ResultManager();
    private Archive archive = null;
    private boolean isPortabilityMode = false;
    private String domainDir = System.getProperty("com.sun.aas.installRoot")+
                                                    File.separator+"domains"+ // NOI18N
                                                    File.separator+"domain1"; // NOI18N
    private String extDir = null;
    private List<String> classPath = null;
    private Application application = null;
    private File jspOutDir = null;

    private String configDirStr = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
            File.separator +
            "lib" + // NOI18N
            File.separator +
            "verifier"; // NOI18N

    private String javaEEVersion = SpecVersionMapper.JavaEEVersion_5;

    /**
     *
     * @return returns true if application tests are enabled with partioning option.
     */
    public boolean isApp() {
        return app;
    }

    /**
     *
     * @return returns true if application client tests are enabled with
     * partioning option.
     */
    public boolean isAppClient() {
        return appClient;
    }

    /**
     *
     * @return returns true if connector tests are enabled with
     * partioning option.
     */
    public boolean isConnector() {
        return connector;
    }

    /**
     *
     * @return returns true if Ejb tests are enabled with
     * partioning option.
     */
    public boolean isEjb() {
        return ejb;
    }

    /**
     *
     * @return returns true if Web tests are enabled with
     * partioning option.
     */
    public boolean isWeb() {
        return web;
    }

    /**
     *
     * @return returns true if Webservices tests are enabled with
     * partioning option.
     */
    public boolean isWebServices() {
        return webServices;
    }

    /**
     *
     * @return returns true if Webservices client tests are enabled with
     * partioning option.
     */
    public boolean isWebServicesClient() {
        return webServicesClient;
    }

    public boolean isPersistenceUnits() {
        return persistenceUnits;
    }

    /**
     *
     * @return returns true if partitioning option is enabled.
     */
    public boolean isPartition() {
        return partition;
    }

    /**
     * If -a option is passed to verifier this variable is set for invoking only
     *  application related tests
     * @param app
     */
    public void setApp(boolean app) {
        this.app = app;
    }

    /**
     * If -p option is passed to verifier this variable is set for invoking only
     *  application client related tests
     * @param appClient
     */
    public void setAppClient(boolean appClient) {
        this.appClient = appClient;
    }

    /**
     * If -c option is passed to verifier this variable is set for invoking only
     *  connector related tests
     * @param connector
     */
    public void setConnector(boolean connector) {
        this.connector = connector;
    }

    /**
     * If -e option is passed to verifier this variable is set for invoking only
     *  ejb related tests
     * @param ejb
     */
    public void setEjb(boolean ejb) {
        this.ejb = ejb;
    }

    /**
     * If -w option is passed to verifier this variable is set for invoking only
     *  web related tests
     * @param web
     */
    public void setWeb(boolean web) {
        this.web = web;
    }

    /**
     * If -s option is passed to verifier this variable is set for invoking only
     *  webServices related tests
     * @param webServices
     */
    public void setWebServices(boolean webServices) {
        this.webServices = webServices;
    }

    /**
     * If -l option is passed to verifier this variable is set for invoking only
     *  webservices client related tests
     * @param webServicesClient
     */
    public void setWebServicesClient(boolean webServicesClient) {
        this.webServicesClient = webServicesClient;
    }

    /**
     * If -P option is passed to verifier this variable is set for invoking only
     *  persistence related tests
     * @param persistenceUnits
     */
    public void setPersistenceUnits(boolean persistenceUnits) {
        this.persistenceUnits = persistenceUnits;
    }

    /**
     * if verifier is invoked to run tests for specific component(s) this
     * variable is set to true
     * @param partition
     */
    public void setPartition(boolean partition) {
        this.partition = partition;
    }

    /**
     *
     * @return return the reporting level of verifier
     */
    public int getReportLevel() {
        return reportLevel;
    }

    /**
     * set the reporting level of verifier
     * @param reportLevel
     */
    public void setReportLevel(int reportLevel) {
        this.reportLevel = reportLevel;
    }

    /**
     *
     * @return return if timestamp is added to the report files
     */
    public boolean isUseTimeStamp() {
        return useTimeStamp;
    }

    /**
     * set option to append timestamp to report files
     * @param useTimeStamp
     */
    public void setUseTimeStamp(boolean useTimeStamp) {
        this.useTimeStamp = useTimeStamp;
    }

    /**
     * get the jar file name to be verifier
     * @return
     */
    public String getJarFileName() {
        return jarFileName;
    }

    /**
     * sets the jar file name to be verified
     * @param jarFileName
     */
    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    /**
     * @return string the output directory where to keep the generated reports
     */
    public String getOutputDirName() {
        return outputDirName;
    }

    /**
     * set the output directory where to keep the report files
     * @param outputDirName
     */
    public void setOutputDirName(String outputDirName) {
        this.outputDirName = outputDirName;
    }

    /**
     *
     * @return the config directory where verifier specific files are kept
     * like Test-Names.xml and xsl files
     */
    public String getConfigDirStr() {
        return configDirStr;
    }


    /**
     * set the config dir.
     * @param configDirStr
     */
    public void setConfigDirStr(String configDirStr) {
        this.configDirStr = configDirStr;
    }

    /**
     *
     * @return The directory where verifier explodes the archive
     */
    public String getExplodedArchivePath() {
        return explodedArchivePath;
    }

    /**
     *
     * @return verifier invokation, in gui mode or command line mode
     */
    public boolean isUsingGui() {
        return usingGui;
    }

    /**
     * @param usingGui set the value to true if verifier is invoked in GUI mode.
     */
    public void setUsingGui(boolean usingGui) {
        this.usingGui = usingGui;
    }

    public boolean isBackend() {
        return isBackend;
    }

    public void setIsBackend(boolean b) {
        this.isBackend = b;
    }
    
    /**
     *
     * @param explodedArchivePath directory path where verifier explodes the archive
     */
    public void setExplodedArchivePath(String explodedArchivePath) {
        this.explodedArchivePath = explodedArchivePath;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public ResultManager getResultManager() {
        return resultManager;
    }

    public boolean isPortabilityMode() {
        return isPortabilityMode;
    }

    public void setPortabilityMode(boolean portabilityMode) {
        isPortabilityMode = portabilityMode;
    }

    public String getDomainDir() {
        return domainDir;
    }

    public void setDomainDir(String domainDir) {
        this.domainDir = domainDir;
    }

    public String getExtDir() {
        return extDir;
    }

    public void setExtDir(String extDir) {
        this.extDir = extDir;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public void setClassPath(List<String> classPath) {
        this.classPath = classPath;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public File getJspOutDir() {
        return jspOutDir;
    }

    public void setJspOutDir(File jspOutDir) {
        this.jspOutDir = jspOutDir;
    }

    public String getJavaEEVersion() {
        return javaEEVersion;
    }

    public void setJavaEEVersion(String javaEEVersion) {
        this.javaEEVersion = javaEEVersion;
    }

}
