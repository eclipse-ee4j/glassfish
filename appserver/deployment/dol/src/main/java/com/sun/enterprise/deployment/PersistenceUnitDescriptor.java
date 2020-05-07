/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * A persistence.xml file can contain one or more <persistence-unit>s
 * This class represents information about a <persistence-unit>.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitDescriptor extends Descriptor {

    private PersistenceUnitsDescriptor parent;

    private String name;

    private String transactionType = "JTA"; // in persistence.xsd default is JTA

    private String description;

    private String provider;

    private String jtaDataSource;

    private String nonJtaDataSource;

    private List<String> mappingFiles = new ArrayList<String>();

    private List<String> jarFiles = new ArrayList<String>();

    private List<String> classes = new ArrayList<String>();

    private Properties properties = new Properties();

    private boolean excludeUnlistedClasses = false;

    private SharedCacheMode sharedCacheMode;

    private ValidationMode validationMode;

    public PersistenceUnitDescriptor() {
    }

    public PersistenceUnitsDescriptor getParent() {
        return parent;
    }

    protected void setParent(PersistenceUnitsDescriptor parent) {
        assert(this.parent==null);
        this.parent = parent;
    }

    // NOW let's implement some methods specific to this descriptor
    // Most of these setter methods are invoked using reflection
    // by PersistenceNode. So any change here has to be reflcted there as
    // well. Compiler won't catch them for you.

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;

    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {

        this.provider = value;
    }

    public String getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(String value) {
        this.jtaDataSource = value;

    }

    public String getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(String value) {
        this.nonJtaDataSource = value;

    }

    public List<String> getMappingFiles() {
        return Collections.unmodifiableList(mappingFiles);
    }

    public void addMappingFile(String mappingFile) {
        mappingFiles.add(mappingFile);
    }

    public List<String> getJarFiles() {
        return Collections.unmodifiableList(jarFiles);
    }

    public void addJarFile(String jarFile) {
        jarFiles.add(jarFile);

    }

    public List<String> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public void addClass(String className) {
        classes.add(className);

    }

    public Properties getProperties() {
        return (Properties) properties.clone();
    }

    public void addProperty(String name, Object value) {
        properties.put(name, value);

    }

    public boolean isExcludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    public void setSharedCacheMode(String sharedCacheMode) {
        // Assume schema validation will only pass a valid value else user would correctly get an exception at deployment
        this.sharedCacheMode = SharedCacheMode.valueOf(sharedCacheMode);
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(String validationMode) {
        // Assume schema validation will only pass a valid value else user would correctly get an exception at deployment
        this.validationMode = ValidationMode.valueOf(validationMode);
    }

    public String getPuRoot() {
        return parent.getPuRoot();
    }

    /**
     * @return the absolute path of the root of this persistence unit
     * @see #getPuRoot()
     * @see PersistenceUnitsDescriptor#getAbsolutePuRoot()
     */
    public String getAbsolutePuRoot() {
        return getParent().getAbsolutePuRoot();
     }

}
