/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;

import static org.glassfish.embeddable.GlassFishVariable.DERBY_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * Driver Loader to load the jdbc drivers and get driver/datasource classnames
 * by introspection.
 *
 * @author Shalini M
 */
@Service
@Singleton
public class DriverLoader implements ConnectorConstants {

    private static Logger logger = LogDomains.getLogger(DriverLoader.class, LogDomains.RSR_LOGGER);

    private static final String DRIVER_INTERFACE_NAME = "java.sql.Driver";
    private static final String SERVICES_DRIVER_IMPL_NAME = "META-INF/services/java.sql.Driver";
    private static final String DATABASE_VENDOR_DERBY = "DERBY";
    private static final String DATABASE_VENDOR_JAVADB = "JAVADB";
    private static final String DATABASE_VENDOR_EMBEDDED_DERBY = "EMBEDDED-DERBY";
    private static final String DATABASE_VENDOR_DERBY_30 = "DERBY-30";
    private static final String DATABASE_VENDOR_EMBEDDED_DERBY_30 = "EMBEDDED-DERBY-30";
    private static final String DATABASE_VENDOR_JAVADB_30 = "JAVADB-30";
    private static final String DATABASE_VENDOR_MSSQLSERVER = "MICROSOFTSQLSERVER";
    private static final String DATABASE_VENDOR_SUN_SQLSERVER = "SUN-SQLSERVER";
    private static final String DATABASE_VENDOR_SUN_ORACLE = "SUN-ORACLE";
    private static final String DATABASE_VENDOR_SUN_DB2 = "SUN-DB2";
    private static final String DATABASE_VENDOR_SUN_SYBASE = "SUN-SYBASE";
    private static final String DATABASE_VENDOR_SYBASE = "SYBASE";
    private static final String DATABASE_VENDOR_ORACLE = "ORACLE";
    private static final String DATABASE_VENDOR_DB2 = "DB2";
    private static final String DATABASE_VENDOR_EMBEDDED = "EMBEDDED";
    private static final String DATABASE_VENDOR_30 = "30";
    private static final String DATABASE_VENDOR_40 = "40";

    private static final String DATABASE_VENDOR_SQLSERVER = "SQLSERVER";
    private static final String DS_PROPERTIES = "ds.properties";
    private static final String CPDS_PROPERTIES = "cpds.properties";
    private static final String XADS_PROPERTIES = "xads.properties";
    private static final String DRIVER_PROPERTIES = "driver.properties";
    private static final String VENDOR_PROPERTIES = "dbvendor.properties";
    private static final String VALIDATIONCLASSNAMES_PROPERTIES = "validationclassnames.properties";


    private File mappingsRoot;

    @PostConstruct
    private void initPaths() {
        this.mappingsRoot = resolveDbVendorMappingRoot();
    }


    private static File resolveDbVendorMappingRoot() {
        File connDbVendorMappingRoot = getSystemPropertyAsFile(DB_VENDOR_MAPPING_ROOT);
        if (connDbVendorMappingRoot != null) {
            return connDbVendorMappingRoot;
        }
        File installRoot = getSystemPropertyAsFile(INSTALL_ROOT.getSystemPropertyName());
        if (installRoot == null) {
            return null;
        }
        File defaultRoot = installRoot.toPath().normalize()
            .resolve(Path.of("lib", "install", "databases", "dbvendormapping")).toFile();
        if (defaultRoot.exists()) {
            return defaultRoot;
        }
        return null;
    }

    /**
     * Get a set of common database vendor names supported in glassfish.
     * @return database vendor names set.
     */
    public Set<String> getDatabaseVendorNames() {
        if (mappingsRoot == null) {
            return Set.of();
        }
        File dbVendorFile = new File(mappingsRoot, VENDOR_PROPERTIES);
        Properties fileProperties = loadFile(dbVendorFile);
        Set<String> dbvendorNames = new TreeSet<>();
        for (String vendor : fileProperties.stringPropertyNames()) {
            dbvendorNames.add(vendor);
        }
        return dbvendorNames;
    }


    public Properties getValidationClassMappingFile() {
        if (mappingsRoot == null) {
            return loadFile(null);
        }
        return loadFile(new File(this.mappingsRoot, VALIDATIONCLASSNAMES_PROPERTIES));
    }

    public String getDatabaseVendorName(String className) {
        if (mappingsRoot == null) {
            return null;
        }
        String dbVendor = getDatabaseVendorName(loadFile(new File(mappingsRoot, DS_PROPERTIES)), className);
        if (dbVendor == null) {
            dbVendor = getDatabaseVendorName(loadFile(new File(mappingsRoot, CPDS_PROPERTIES)), className);
        }
        if (dbVendor == null) {
            dbVendor = getDatabaseVendorName(loadFile(new File(mappingsRoot, XADS_PROPERTIES)), className);
        }
        if (dbVendor == null) {
            dbVendor = getDatabaseVendorName(loadFile(new File(mappingsRoot, DRIVER_PROPERTIES)), className);
        }
        return dbVendor;
    }

    private File getResourceTypeFile(String resType) {
        if (mappingsRoot == null) {
            return null;
        }
        if (ConnectorConstants.JAVAX_SQL_DATASOURCE.equals(resType)) {
            return new File(mappingsRoot, DS_PROPERTIES);
        } else if (ConnectorConstants.JAVAX_SQL_XA_DATASOURCE.equals(resType)) {
            return new File(mappingsRoot, XADS_PROPERTIES);
        } else if (ConnectorConstants.JAVAX_SQL_CONNECTION_POOL_DATASOURCE.equals(resType)) {
            return new File(mappingsRoot, CPDS_PROPERTIES);
        } else if (ConnectorConstants.JAVA_SQL_DRIVER.equals(resType)) {
            return new File(mappingsRoot, DRIVER_PROPERTIES);
        } else {
            return null;
        }
    }

    private String getDatabaseVendorName(Properties classNameProperties, String className) {
        Set<String> vendors = classNameProperties.stringPropertyNames();
        for (String vendor : vendors) {
            String value = classNameProperties.getProperty(vendor);
            if (className.equalsIgnoreCase(value)) {
                return vendor;
            }
        }
        return null;
    }

    public static Properties loadFile(File mappingFile) {
        Properties fileProperties = new Properties();
        if (mappingFile == null || !mappingFile.exists()) {
            logger.fine(() -> "File not found: " + mappingFile);
            return fileProperties;
        }
        try (FileInputStream fis = new FileInputStream(mappingFile)) {
            fileProperties.load(fis);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "IO Exception during properties load: " + mappingFile);
        }
        return fileProperties;
    }


    private String getImplClassNameFromMapping(String dbVendor, String resType) {
        File mappingFile = getResourceTypeFile(resType);
        Properties fileProperties = loadFile(mappingFile);
        return fileProperties.getProperty(dbVendor.toUpperCase(Locale.getDefault()));
    }

    /**
     * Get equivalent name for the database vendor name. This is useful for
     * introspection as the vendor name for oracle and sun oracle type of jdbc
     * drivers are the same.
     * @param dbVendor
     * @return
     */
    private String getEquivalentName(String dbVendor) {
        if (dbVendor.toUpperCase(Locale.getDefault()).startsWith(DATABASE_VENDOR_JAVADB) ||
                dbVendor.equalsIgnoreCase(DATABASE_VENDOR_EMBEDDED_DERBY) ||
                dbVendor.equalsIgnoreCase(DATABASE_VENDOR_EMBEDDED_DERBY_30) ||
                dbVendor.equalsIgnoreCase(DATABASE_VENDOR_DERBY_30) ||
                dbVendor.equalsIgnoreCase(DATABASE_VENDOR_JAVADB_30)) {
            return DATABASE_VENDOR_DERBY;
        } else if (dbVendor.equalsIgnoreCase(DATABASE_VENDOR_MSSQLSERVER) ||
                dbVendor.equalsIgnoreCase(DATABASE_VENDOR_SUN_SQLSERVER)) {
            return DATABASE_VENDOR_SQLSERVER;
        } else if (dbVendor.equalsIgnoreCase(DATABASE_VENDOR_SUN_DB2)) {
            return DATABASE_VENDOR_DB2;
        } else if (dbVendor.equalsIgnoreCase(DATABASE_VENDOR_SUN_ORACLE)) {
            return DATABASE_VENDOR_ORACLE;
        } else if (dbVendor.equalsIgnoreCase(DATABASE_VENDOR_SUN_SYBASE)) {
            return DATABASE_VENDOR_SYBASE;
        }
        return null;
    }

    public Set<String> getJdbcDriverClassNames(String dbVendor, String resType) {
        //Do not use introspection by default.
        return getJdbcDriverClassNames(dbVendor, resType, false);
    }

    /**
     * Gets a set of driver or datasource classnames for the particular vendor.
     * Loads the jdbc driver, introspects the jdbc driver jar and gets the
     * classnames.
     * Based on whether introspect flag is turned on or off, the classnames are
     * retrieved by introspection or from a pre-defined list.
     * @return
     */
    public Set<String> getJdbcDriverClassNames(String dbVendor, String resType,
            boolean introspect) {
        //Map of all jar files with the set of driver implementations. every file
        // that is a jdbc jar will have a set of driver impls.
        Set<String> implClassNames = new TreeSet<>();
        Set<String> allImplClassNames = new TreeSet<>();
        //Used for introspection.
        String vendor = null;

        if(dbVendor != null) {
            dbVendor = dbVendor.trim().replaceAll(" ", "");
            vendor = getEquivalentName(dbVendor);
            if (vendor == null) {
                vendor = dbVendor;
            }
        }

        if(!introspect) {
            //Get from the pre-populated list. This is done for common dbvendor names
            String implClass = getImplClassNameFromMapping(dbVendor, resType);

            if(implClass != null) {
                allImplClassNames.add(implClass);
                return allImplClassNames;
            }
        }

        List<File> jarFileLocations = getJdbcDriverLocations();
        Set<File> allJars = new HashSet<>();
        for(File lib : jarFileLocations) {
            if (lib.isDirectory()) {
                File[] files = lib.listFiles((dir, name) -> name.endsWith(".jar"));
                if (files != null) {
                    for (File file : files) {
                        allJars.add(file);
                    }
                }
            }
        }
        for (File file : allJars) {
            if (file.isFile()) {
                //Introspect jar and get classnames.
                if(vendor != null) {
                    implClassNames = introspectAndLoadJar(file, resType, vendor, dbVendor);
                }
                //Found the impl classnames for the particular dbVendor.
                //Hence no need to search in other jar files.
                if(!implClassNames.isEmpty()) {
                    for(String className : implClassNames) {
                        allImplClassNames.add(className);
                    }
                }
            }
        }

        return allImplClassNames;
    }

    private Set<String> getImplClassesByIteration(File f, String resType,
            String dbVendor, String origDbVendor) {
        SortedSet<String> implClassNames = new TreeSet<>();
        String implClass = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Enumeration<JarEntry> e = jarFile.entries();
            while(e.hasMoreElements()) {

                JarEntry jarEntry = e.nextElement();

                if (jarEntry != null) {

                    String entry = jarEntry.getName();
                    if (DRIVER_INTERFACE_NAME.equals(resType)) {
                        if (SERVICES_DRIVER_IMPL_NAME.equals(entry)) {

                            InputStream metaInf = jarFile.getInputStream(jarEntry);
                            implClass = processMetaInf(metaInf);
                            if (implClass != null) {
                                if (isLoaded(implClass, resType)) {
                                    //Add to the implClassNames only if vendor name matches.
                                    if(isVendorSpecific(f, dbVendor, implClass, origDbVendor)) {
                                        implClassNames.add(implClass);
                                    }
                                }
                            }
                            if(logger.isLoggable(Level.FINEST)) {
                                logger.finest("Driver loader : implClass = " + implClass);
                            }

                        }
                    }
                    if (entry.endsWith(".class")) {
                        //Read from metainf file for all jdbc40 drivers and resType
                        //java.sql.Driver.TODO : this should go outside .class check.
                        //TODO : Some classnames might not have these strings
                        //in their classname. Logic should be flexible in such cases.
                        if (entry.toUpperCase(Locale.getDefault()).indexOf("DATASOURCE") != -1 ||
                                entry.toUpperCase(Locale.getDefault()).indexOf("DRIVER") != -1) {
                            implClass = getClassName(entry);
                            if (isLoaded(implClass, resType)) {
                                if (isVendorSpecific(f, dbVendor, implClass, origDbVendor)) {
                                    implClassNames.add(implClass);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error while getting Jdbc driver classnames ", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Exception while closing JarFile '"
                                + jarFile.getName() + "' :", ex);
                    }
                }
            }
        }
        //Could be one or many depending on the connection definition class name
        return implClassNames;
    }

    /**
     * Returns a list of all driver class names that were loaded from the jar file.
     * @param f
     * @param dbVendor
     * @return Set of driver/datasource class implementations based on resType
     */
    private Set<String> introspectAndLoadJar(File f, String resType,
            String dbVendor, String origDbVendor) {

        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("DriverLoader : introspectAndLoadJar ");
        }

        return getImplClassesByIteration(f, resType, dbVendor, origDbVendor);

    }

    private boolean isNotAbstract(Class cls) {
        int modifier = cls.getModifiers();
        return !Modifier.isAbstract(modifier);
    }

    /**
     * Reads the META-INF/services/java.sql.Driver file contents and returns
     * the driver implementation class name.
     * In case of jdbc40 drivers, the META-INF/services/java.sql.Driver file
     * contains the name of the driver class.
     * @param metaInf
     * @return driver implementation class name
     */
    private String processMetaInf(InputStream metaInf) {
        String driverClassName = null;
        InputStreamReader reader = null;
        BufferedReader buffReader = null;
        try {
            reader = new InputStreamReader(metaInf);
            buffReader = new BufferedReader(reader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                driverClassName = line;
            }
        } catch(IOException ioex) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("DriverLoader : exception while processing "
                        + "META-INF directory for DriverClassName " + ioex);
            }
        } finally {
            try {
                if(buffReader != null) {
                    buffReader.close();
                }
            } catch (IOException ex) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Error while closing File handles after reading META-INF files : ", ex);
                }
            }
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Error while closing File handles after reading META-INF files : ", ex);
                }
            }
        }
        return driverClassName;
    }

    /**
     * Check if the classname has been loaded and if it is a Driver or a
     * DataSource impl.
     * @param classname
     * @return
     */
    private boolean isLoaded(String classname, String resType) {
        Class cls = null;
        try {
            //This will fail in case the driver is not in classpath.
            cls = ConnectorRuntime.getRuntime().getConnectorClassLoader().loadClass(classname);
        //Check shud be made here to look into the lib directory now to see
        // if there are any newly installed drivers.
        //If so, create a URLClassLoader and load the class with common
        //classloader as the parent.
        } catch (Exception ex) {
            cls = null;
        } catch (Throwable t) {
            cls = null;
        }
        return (isResType(cls, resType));
    }

    /**
     * Find if the particular class has any implementations of java.sql.Driver or
     * javax.sql.DataSource or any other resTypes passed.
     * @param cls
     * @return
     */
    private boolean isResType(Class cls, String resType) {
        boolean isResType = false;
        if (cls != null) {
            if("javax.sql.DataSource".equals(resType)) {
                if(javax.sql.DataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if("javax.sql.ConnectionPoolDataSource".equals(resType)) {
                if(javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if("javax.sql.XADataSource".equals(resType)) {
                if(javax.sql.XADataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if("java.sql.Driver".equals(resType)) {
                if(java.sql.Driver.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            }
        }
        return isResType;
    }


    private String getClassName(String classname) {
        classname = classname.replaceAll("/", ".");
        classname = classname.substring(0, classname.lastIndexOf(".class"));
        return classname;
    }


    private boolean isVendorSpecific(File f, String dbVendor, String className, String origDbVendor) {
        //File could be a jdbc jar file or a normal jar file
        boolean isVendorSpecific = false;

        if(origDbVendor != null) {
            if(origDbVendor.equalsIgnoreCase(DATABASE_VENDOR_EMBEDDED_DERBY)) {
                return className.toUpperCase(Locale.getDefault()).indexOf(DATABASE_VENDOR_EMBEDDED) != -1;
            } else if(origDbVendor.equalsIgnoreCase(DATABASE_VENDOR_EMBEDDED_DERBY_30)) {
                if(className.toUpperCase(Locale.getDefault()).indexOf(DATABASE_VENDOR_EMBEDDED) != -1) {
                    if(origDbVendor.endsWith(DATABASE_VENDOR_30)) {
                        return !(className.toUpperCase(Locale.getDefault()).endsWith(DATABASE_VENDOR_40));
                    }
                }
            }
        }

        String vendor = getVendorFromManifest(f);

        if (vendor == null) {
            //might have to do this part by going through the class names or some other method.
            //dbVendor might be used in this portion
            if (isVendorSpecific(dbVendor, className)) {
                isVendorSpecific = true;
            }
        } else //Got from Manifest file.
        if (vendor.equalsIgnoreCase(dbVendor) ||
                vendor.toUpperCase(Locale.getDefault()).indexOf(
                dbVendor.toUpperCase(Locale.getDefault())) != -1) {
            isVendorSpecific = true;
        }
        if(isVendorSpecific) {
            if(origDbVendor.endsWith(DATABASE_VENDOR_30)) {
                if(origDbVendor.equalsIgnoreCase(DATABASE_VENDOR_EMBEDDED_DERBY_30)) {
                    return className.toUpperCase(Locale.getDefault()).indexOf(DATABASE_VENDOR_EMBEDDED) != -1;
                }
                return !(className.toUpperCase(Locale.getDefault()).endsWith(DATABASE_VENDOR_40));
            }
        }
        return isVendorSpecific;
    }

    private List<File> getJdbcDriverLocations() {
        List<File> jarFileLocations = new ArrayList<>();
        File derby = getLocation(DERBY_ROOT.getSystemPropertyName());
        if (derby != null) {
            jarFileLocations.add(derby);
        }
        File installRoot = getLocation(INSTALL_ROOT.getSystemPropertyName());
        if (installRoot != null) {
            jarFileLocations.add(installRoot);
        }
        File instanceRoot = getLocation(INSTANCE_ROOT.getSystemPropertyName());
        if (instanceRoot != null) {
            jarFileLocations.add(instanceRoot);
        }
        return jarFileLocations;
    }

    private File getLocation(String property) {
        File directory = getSystemPropertyAsFile(property);
        if (directory == null) {
            return null;
        }
        return new File(directory, "lib");
    }


    /**
     * @return null if the file does not exist or the property is not set
     */
    private static File getSystemPropertyAsFile(String property) {
        String propertyValue = System.getProperty(property);
        if (propertyValue == null || propertyValue.isBlank()) {
            return null;
        }
        File file = new File(propertyValue);
        return file.exists() ? file : null;
    }


    /**
     * Utility method that checks if a classname is vendor specific.
     * This method is used for jar files that do not have a manifest file to
     * look up the classname.
     * @param dbVendor
     * @param className
     * @return true if className is vendor specific.
     */
    private boolean isVendorSpecific(String dbVendor, String className) {
        return className.toUpperCase(Locale.getDefault()).indexOf(
                dbVendor.toUpperCase(Locale.getDefault())) != -1;
    }

    /**
     * Get a vendor name from a Manifest entry in the file.
     * @param f
     * @return null if no manifest entry found.
     */
    private String getVendorFromManifest(File f) {
        try (JarFile jarFile = new JarFile(f)) {
            final Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            final Attributes mainAttributes = manifest.getMainAttributes();
            if (mainAttributes == null) {
                return null;
            }
            return mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception while reading manifest file : ", ex);
            return null;
        }
    }
}
