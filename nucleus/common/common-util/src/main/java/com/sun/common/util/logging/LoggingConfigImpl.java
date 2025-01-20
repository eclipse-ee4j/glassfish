/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.common.util.logging;

import jakarta.inject.Inject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.glassfish.api.admin.FileMonitoring;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.formatter.ODLLogFormatter;
import org.glassfish.main.jul.handler.GlassFishLogHandlerProperty;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ENABLED;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.FORMATTER;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.OUTPUT_FILE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.REDIRECT_STANDARD_STREAMS;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_LIMIT_SIZE;

/**
 * Implementation of Logging Commands
 *
 * @author Naman Mehta
 */
@Service
@Contract
public class LoggingConfigImpl implements LoggingConfig, PostConstruct {

    private static final Logger LOG = Logger.getLogger(LoggingConfigImpl.class.getName());

    private static final String DEFAULT_SERVER_LOG_PATH = "${com.sun.aas.instanceRoot}/logs/server.log";

    private static final Map<String, String> DEFAULT_LOG_PROPERTIES = new HashMap<>();
    static {
        DEFAULT_LOG_PROPERTIES.put(ENABLED.getPropertyFullName(), "true");
        DEFAULT_LOG_PROPERTIES.put(OUTPUT_FILE.getPropertyFullName(), DEFAULT_SERVER_LOG_PATH);
        DEFAULT_LOG_PROPERTIES.put(FORMATTER.getPropertyFullName(), ODLLogFormatter.class.getName());
        DEFAULT_LOG_PROPERTIES.put(REDIRECT_STANDARD_STREAMS.getPropertyFullName(), "true");
        DEFAULT_LOG_PROPERTIES.put(ROTATION_LIMIT_SIZE.getPropertyFullName(), "2");
    }


    @Inject
    ServerEnvironmentImpl env;

    @Inject
    FileMonitoring fileMonitoring;

    LoggingProperties props = new LoggingProperties();
    String loggingPropertiesName;
    File loggingConfigDir = null;

    @Override
    public void postConstruct() {
        // set logging.properties filename
        setupConfigDir(env.getConfigDirPath(), env.getLibPath());
    }


    @SuppressWarnings("unused")
    public void setupConfigDir(File file, File installDir) {
        loggingConfigDir = file;
        loggingPropertiesName = ServerEnvironmentImpl.kLoggingPropertiesFileName;
    }


    /**
     * Load the properties for DAS
     */
    private void loadLoggingProperties() throws IOException {
        props = new LoggingProperties();
        File file = getLoggingPropertiesFile();
        try (InputStream fis = openPropertyFile(file)) {
            props.load(fis);
        }
    }


    /**
     * Load the properties for given target.
     */
    private void loadLoggingProperties(String target) throws IOException {
        props = new LoggingProperties();
        File file = getLoggingPropertiesFile(target);
        try (InputStream fis = openPropertyFile(file)) {
            props.load(fis);
        }
    }


    private InputStream openPropertyFile(File file) throws IOException {
        final FileInputStream fis;
        if (file.exists()) {
            fis = new FileInputStream(file);
        } else {
            fis = getDefaultLoggingPropertiesInputStream();
        }
        return new BufferedInputStream(fis);
    }

    private File getLoggingPropertiesFile() {
        return new File(loggingConfigDir, loggingPropertiesName);
    }

    private File getLoggingPropertiesFile(String target) {
        String pathForLoggingFile = loggingConfigDir.getAbsolutePath();
        // target param  is null e.g. for DAS server
        if (target != null) {
            pathForLoggingFile += File.separator + target;
        }
        return new File(pathForLoggingFile, ServerEnvironmentImpl.kLoggingPropertiesFileName);
    }


    private FileInputStream getDefaultLoggingPropertiesInputStream() throws IOException {
        File defaultConfig = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kDefaultLoggingPropertiesFileName);
        return new FileInputStream(defaultConfig);
    }

    private void safeCloseStream(OutputStream os) {
        try {
            if(os != null) {
                os.close();
            }
        } catch (Exception e) {
            // nothing can be done about it...
        }
    }
    private void safeCloseStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            // nothing can be done about it...
        }
    }

    private void closePropFile(String targetConfigName) throws IOException {
        OutputStream os = null;
        try {
            File file;
            if (targetConfigName == null || targetConfigName.isEmpty()) {
                file = getLoggingPropertiesFile();
            } else {
                file = getLoggingPropertiesFile(targetConfigName);
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IOException();
            }
            os = new BufferedOutputStream(new FileOutputStream(file));
            props.store(os, "GlassFish logging.properties list");
            os.flush();
            fileMonitoring.fileModified(file);
        } finally {
            safeCloseStream(os);
        }
    }

    private void setWebLoggers(String value) {
        // set the rest of the web loggers to the same level
        // these are only accessible via the web-container name so all values should be the same
        props.setProperty("org.apache.catalina.level", value);
        props.setProperty("org.apache.coyote.level", value);
        props.setProperty("org.glassfish.wasp.level", value);
    }

    /**
     * setLoggingProperty() sets an existing propertyName to be propertyValue
     * if the property doesn't exist the property will be added.  The logManager
     * readConfiguration is not called in this method.
     *
     * @param propertyName  Name of the property to set
     * @param propertyValue Value to set
     * @throws IOException If an I/O error occurs
     */
    @Override
    public synchronized String setLoggingProperty(String propertyName, String propertyValue) throws IOException {
        loadLoggingProperties();
        // update the property
        if (propertyValue == null) {
            return null;
        }
        // may need to map the domain.xml name to the new name in logging.properties file
        String key = LoggingXMLNames.xmltoPropsMap.get(propertyName);
        if (key == null) {
            key = propertyName;
        }
        String property = (String) props.setProperty(key, propertyValue);
        if (propertyName.contains("jakarta.enterprise.system.container.web")) {
            setWebLoggers(propertyValue);
        }

        closePropFile(null);
        return property;
    }

    /**
     * setLoggingProperty() sets an existing propertyName to be propertyValue
     * if the property doesn't exist the property will be added.  The logManager
     * readConfiguration is not called in this method.
     *
     * @param propertyName  Name of the property to set
     * @param propertyValue Value to set
     * @throws IOException If an I/O error occurs
     */
    @Override
    public synchronized String setLoggingProperty(String propertyName, String propertyValue, String targetConfigName) throws IOException {
        loadLoggingProperties(targetConfigName);
        // update the property
        if (propertyValue == null) {
            return null;
        }
        // may need to map the domain.xml name to the new name in logging.properties file
        String key = LoggingXMLNames.xmltoPropsMap.get(propertyName);
        if (key == null) {
            key = propertyName;
        }
        String property = (String) props.setProperty(key, propertyValue);
        if (propertyName.contains("jakarta.enterprise.system.container.web")) {
            setWebLoggers(propertyValue);
        }

        closePropFile(targetConfigName);
        return property;
    }

    /** update the properties to new values.  properties is a Map of names of properties and
      * their corresponding value.  If the property does not exist then it is added to the
      * logging.properties file.
      *
      * @param properties Map of the name and value of property to add or update
      *
      * @throws IOException If an I/O error occurs
      */

    @Override
    public synchronized Map<String, String> updateLoggingProperties(Map<String, String> properties) throws IOException {
        Map<String, String> m = new HashMap<>();
        loadLoggingProperties();
        // need to map the name given to the new name in logging.properties file
        String key;
        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            key = LoggingXMLNames.xmltoPropsMap.get(e.getKey());
            if (key == null) {
                key = e.getKey();
            }
            String property = (String) props.setProperty(key, e.getValue());
            if (e.getKey().contains("jakarta.enterprise.system.container.web")) {
                setWebLoggers(e.getValue());
            }

            // build Map of entries to return
            m.put(key, property);

        }
        closePropFile(null);
        return m;
    }


    @Override
    public synchronized Map<String, String> updateLoggingProperties(Map<String, String> properties, String targetConfigName) throws IOException {
        Map<String, String> m = new HashMap<>();
        loadLoggingProperties(targetConfigName);
        // need to map the name given to the new name in logging.properties file
        String key;
        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            key = LoggingXMLNames.xmltoPropsMap.get(e.getKey());
            if (key == null) {
                key = e.getKey();
            }
            String property = (String) props.setProperty(key, e.getValue());
            if (e.getKey().contains("jakarta.enterprise.system.container.web")) {
                setWebLoggers(e.getValue());
            }

            //build Map of entries to return
            m.put(key, property);

        }
        closePropFile(targetConfigName);
        return m;
    }

    /** Return a Map of all the properties and corresponding values in the logging.properties file for given target.
     *
     * @param targetConfigName Target config name
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public synchronized Map<String, String> getLoggingProperties(String targetConfigName) throws IOException {
        Map<String, String> m = new HashMap<>();

        loadLoggingProperties(targetConfigName);
        Enumeration<?> e = props.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            // convert the name in domain.xml to the name in logging.properties if needed
            if (LoggingXMLNames.xmltoPropsMap.get(key) != null) {
                key = LoggingXMLNames.xmltoPropsMap.get(key);
            }

            m.put(key, props.getProperty(key));
        }
        return m;
    }

    /** Return a Map of all the properties and corresponding values in the logging.properties file.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public synchronized Map<String, String> getLoggingProperties() throws IOException {
        Map<String, String> m = new HashMap<>();

        loadLoggingProperties();

        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            // convert the name in domain.xml to the name in logging.properties if needed
            if (LoggingXMLNames.xmltoPropsMap.get(key) != null) {
                key = LoggingXMLNames.xmltoPropsMap.get(key);
            }

            m.put(key, props.getProperty(key));
        }
        return m;
    }


    @Override
    public synchronized Map<String, String> deleteLoggingProperties(final Collection<String> properties)
        throws IOException {
        LOG.log(Level.INFO, "deleteLoggingProperties(properties={0})", properties);
        loadLoggingProperties();
        remove(properties);
        rewritePropertiesFileAndNotifyMonitoring(null);
        return setMissingDefaults(getLoggingProperties());
    }


    @Override
    public synchronized Map<String, String> deleteLoggingProperties(final Collection<String> properties,
        final String target) throws IOException {
        LOG.log(Level.INFO, "deleteLoggingProperties(properties={0}, target={1})", new Object[] {properties, target});
        loadLoggingProperties(target);
        remove(properties);
        rewritePropertiesFileAndNotifyMonitoring(target);
        return setMissingDefaults(getLoggingProperties(target));
    }

    private void remove(final Collection<String> keys) {
        final Consumer<String> toRealKeyAndDelete = k -> props.remove(LoggingXMLNames.xmltoPropsMap.getOrDefault(k, k));
        keys.forEach(toRealKeyAndDelete);
    }

    private void rewritePropertiesFileAndNotifyMonitoring(final String targetConfigName) throws IOException {
        LOG.finest("rewritePropertiesFileAndNotifyMonitoring");
        final File file;
        if (targetConfigName == null || targetConfigName.isEmpty()) {
            file = getLoggingPropertiesFile();
        } else {
            file = getLoggingPropertiesFile(targetConfigName);
        }
        final File parentFile = file.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException(
                "Directory '" + parentFile + "' does not exist, cannot create logging.properties file!");
        }
        try {
            new LoggingProperties(props).store(file, "GlassFish logging.properties list");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not store " + file, e);
        }
        fileMonitoring.fileModified(file);
    }

    private Map<String, String> setMissingDefaults(Map<String, String> loggingProperties) {
        for (Entry<String, String> entry : DEFAULT_LOG_PROPERTIES.entrySet()) {
            if (!loggingProperties.containsKey(entry.getKey())) {
                loggingProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return loggingProperties;
    }


    /**
     * @param sourceDir Directory underneath zip file should be created.
     * @return the zip File Name to create for collection log files
     */
    private String getZipFileName(String sourceDir) {

        final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH-mm-ss";

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String currentTime = sdf.format(cal.getTime());

        String zipFile = sourceDir + File.separator + "log_" + currentTime + ".zip";

        return zipFile;
    }

    /**
     * Returns the zip File Name to create for collection log files
     *
     * @param sourceDir Directory underneath zip file should be created.
     * @param fileName file name for zip file
     */
    private String getZipFileName(String sourceDir, String fileName) {

        final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH-mm-ss";

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String currentTime = sdf.format(cal.getTime());

        String zipFile = sourceDir + File.separator + fileName + "-" + currentTime + ".zip";

        return zipFile;
    }

    /**
     * Creating zip file for given log files
     *
     * @param sourceDir Source directory from which needs to create zip
     *
     * @throws  IOException if an I/O error occurs
     */
    @Override
    public String createZipFile(String sourceDir) throws IOException {
        ZipOutputStream zout = null;
        String zipFile = getZipFileName(sourceDir);
        try {
            //create object of FileOutputStream
            FileOutputStream fout = new FileOutputStream(zipFile);

            //create object of ZipOutputStream from FileOutputStream
            zout = new ZipOutputStream(fout);

            //create File object from source directory
            File fileSource = new File(sourceDir);

            addDirectory(zout, fileSource,
                    fileSource.getAbsolutePath().length() + 1);

            //close the ZipOutputStream
            zout.close();
        } catch (IOException ioe) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while creating zip file :", ioe);
            throw ioe;
        } finally {
            safeCloseStream(zout);
        }
        return zipFile;
    }

    /**
     * Creating zip file for given log files
     *
     * @param sourceDir Source directory from which needs to create zip
     * @param zipFileName zip file name which need to be created
     *
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("unused")
    public String createZipFile(String sourceDir, String zipFileName) throws IOException {
        ZipOutputStream zout = null;
        String zipFile = getZipFileName(sourceDir, zipFileName);
        try {
            //create object of FileOutputStream
            FileOutputStream fout = new FileOutputStream(zipFile);

            //create object of ZipOutputStream from FileOutputStream
            zout = new ZipOutputStream(fout);

            //create File object from source directory
            File fileSource = new File(sourceDir);

            addDirectory(zout, fileSource, fileSource.getAbsolutePath().length() + 1);

            //close the ZipOutputStream
            zout.close();
        } catch (IOException ioe) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while creating zip file :", ioe);
            throw ioe;
        } finally {
            safeCloseStream(zout);
        }
        return zipFile;
    }


    /**
     * Helper method to creating zip.
     *
     * @param zout ZipOutputStream which points to zip file
     * @param  fileSource File which needs to add under zip
     *
     * @throws IOException If an I/O error occurs
     */
    private void addDirectory(ZipOutputStream zout, File fileSource, int ignoreLength) throws IOException {
        //get sub-folder/files list
        File[] files = Objects.requireNonNull(fileSource.listFiles());
        FileInputStream fin = null;
        for (File file : files) {
            //if the file is directory, call the function recursively
            if (file.isDirectory()) {
                addDirectory(zout, file, ignoreLength);
                continue;
            }

            if (file.getAbsolutePath().contains(".zip")) {
                continue;
            }
            /*
            * we are here means, its file and not directory, so
            * add it to the zip file
            */
            try {
                //create byte buffer
                byte[] buffer = new byte[1024];

                //create object of FileInputStream
                fin = new FileInputStream(file.getAbsolutePath());
                zout.putNextEntry(new ZipEntry(ignoreLength > -1 ?
                        file.getAbsolutePath().substring(ignoreLength) :
                        file.getAbsolutePath()));

                /*
                * After creating entry in the zip file, actually
                * write the file.
                */
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }

                /*
                 * After writing the file to ZipOutputStream, use
                 * void closeEntry() method of ZipOutputStream class to
                 * close the current entry and position the stream to
                 * write the next entry.
                 */
                zout.closeEntry();

            } catch (IOException ioe) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Error while creating zip file :", ioe);
                throw ioe;
            } finally {
                safeCloseStream(fin);
            }
        }
    }

    /**
     * @return a logging file path from the logging.properties file.
     * @throws IOException If an I/O error occurs
     */
    public synchronized String getLoggingFileDetails() throws IOException {
        LOG.finest("getLoggingFileDetails()");
        loadLoggingProperties();

        @SuppressWarnings("unchecked")
        Enumeration<String> loggingPropertyNames = (Enumeration<String>) props.propertyNames();
        while (loggingPropertyNames.hasMoreElements()) {
            String key = loggingPropertyNames.nextElement();

            // Convert the name in domain.xml to the name in logging.properties if needed
            key = LoggingXMLNames.xmltoPropsMap.getOrDefault(key, key);
            if (GlassFishLogHandlerProperty.OUTPUT_FILE.getPropertyFullName().equals(key)) {
                return props.getProperty(key);
            }
        }

        return null;
    }


    /**
     * @return a logging file path in the logging.properties file for given target.
     * @throws IOException If an I/O error occurs
     */
    public synchronized String getLoggingFileDetails(String targetConfigName) throws IOException {
        loadLoggingProperties(targetConfigName);
        @SuppressWarnings("unchecked")
        Enumeration<String> loggingPropertyNames = (Enumeration<String>) props.propertyNames();
        while (loggingPropertyNames.hasMoreElements()) {
            String key = loggingPropertyNames.nextElement();

            // convert the name in domain.xml to the name in logging.properties if needed
            key = LoggingXMLNames.xmltoPropsMap.getOrDefault(key, key);
            if (GlassFishLogHandlerProperty.OUTPUT_FILE.getPropertyFullName().equals(key)) {
                return props.getProperty(key);
            }
        }
        return null;
    }


    /**
     * @return a Map of all the properties and corresponding values from the logging.properties file
     *         from template.
     * @throws IOException If an I/O error occurs
     */
    public Map<String, String> getDefaultLoggingProperties() throws IOException {
        FileInputStream fisForLoggingTemplate = null;
        Properties propsLoggingTemplate = new Properties();
        Map<String, String> m = new HashMap<>();
        try {
            File loggingTemplateFile = new File(env.getConfigDirPath(),
                ServerEnvironmentImpl.kDefaultLoggingPropertiesFileName);
            fisForLoggingTemplate = new FileInputStream(loggingTemplateFile);
            propsLoggingTemplate.load(fisForLoggingTemplate);
        } finally {
            safeCloseStream(fisForLoggingTemplate);
        }

        Enumeration<?> e = propsLoggingTemplate.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            // convert the name in domain.xml to the name in logging.properties if needed
            if (LoggingXMLNames.xmltoPropsMap.get(key) != null) {
                key = LoggingXMLNames.xmltoPropsMap.get(key);
            }
            m.put(key, propsLoggingTemplate.getProperty(key));
        }
        return m;
    }
}
