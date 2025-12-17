/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;

/**
 * A persistence.xml file can contain one or more <persistence-unit>s
 * This class represents information about a <persistence-unit>.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;

    private PersistenceUnitsDescriptor parent;

    private String name;
    /** in persistence.xsd default is JTA */
    private String transactionType = "JTA";
    private String description;
    private String provider;
    private String scope;
    private SimpleJndiName jtaDataSource;
    private SimpleJndiName nonJtaDataSource;

    private final List<String> mappingFiles = new ArrayList<>();
    private final List<String> jarFiles = new ArrayList<>();
    private final List<String> classes = new ArrayList<>();
    private final List<String> qualifiers = new ArrayList<>();
    private final Properties properties = new Properties();

    private boolean excludeUnlistedClasses;
    private SharedCacheMode sharedCacheMode;
    private ValidationMode validationMode;


    public PersistenceUnitsDescriptor getParent() {
        return parent;
    }

    protected void setParent(PersistenceUnitsDescriptor parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent was already set to " + this.parent.getModuleID()
                + ", it cannot be set to " + parent.getModuleID());
        }
        this.parent = parent;
    }

    public final SimpleJndiName getJndiName() {
        return SimpleJndiName.of(getName());
    }

    // NOW let's implement some methods specific to this descriptor
    // Most of these setter methods are invoked using reflection
    // by PersistenceNode. So any change here has to be reflected there as
    // well. Compiler won't catch them for you.

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String value) {
        this.name = value;

    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        this.provider = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public SimpleJndiName getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(SimpleJndiName value) {
        this.jtaDataSource = value;

    }

    public SimpleJndiName getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(SimpleJndiName value) {
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

    public List<String> getQualifiers() {
        return Collections.unmodifiableList(qualifiers);
    }
    public void addQualifier(String qualifier) {
        qualifiers.add(qualifier);
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
