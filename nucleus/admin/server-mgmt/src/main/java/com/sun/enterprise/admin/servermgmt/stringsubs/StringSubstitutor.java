/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Component;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Group;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Property;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.PropertyType;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

import java.io.File;
import java.util.List;

/**
 * An object which allows to set the custom behavior for string substitution operation and facilitate String
 * substitution process.
 * <p>
 * String substitution is a process of substituting a string in a file with another string.
 * </p>
 */
public interface StringSubstitutor {
    /**
     * Set's the {@link AttributePreprocessor} to customize the substitution process. Attribute preprocessor takes care to
     * retrieve the value of substitutable key.
     *
     * @param attributePreprocessor Custom implementation of {@link AttributePreprocessor}
     */
    void setAttributePreprocessor(AttributePreprocessor attributePreprocessor);

    /**
     * Set's a factory which can process a {@link FileEntry} or an {@link Archive} entry to retrieve all the
     * {@link Substitutable} entries.
     *
     * @param factory
     */
    void setEntryFactory(SubstitutableFactory factory);

    /**
     * TODO: Missing Implementation
     *
     * @param backupLocation
     */
    void setFileBackupLocation(File backupLocation);

    /**
     * Get's the default {@link Property} for the given {@link PropertyType}, If the property type is null then all the
     * default properties will be returned.
     *
     * @param type The type for which default properties has to be retrieved.
     * @return List of default properties or empty list if no property found.
     */
    List<Property> getDefaultProperties(PropertyType type);

    /**
     * Get's the string-subs definition object. A {@link StringSubsDefiniton} object contains the details of component,
     * groups and files used in substitution.
     *
     * <p>
     * <b>NOTE</b>: This object is updatable.
     * </p>
     *
     * @return Parsed string-subs configuration object.
     */
    StringsubsDefinition getStringSubsDefinition();

    /**
     * Perform's string substitution.
     *
     * @throws StringSubstitutionException If any error occurs in string substitution.
     */
    void substituteAll() throws StringSubstitutionException;

    /**
     * Perform's string substitution for give components.
     *
     * @param component List of {@link Component} identifiers for which the string substitution has to be performed.
     * @throws StringSubstitutionException If any error occurs during substitution.
     */
    void substituteComponents(List<String> components) throws StringSubstitutionException;

    /**
     * Perform's string substitution for give groups.
     *
     * @param groups List of {@link Group} identifiers for which the string substitution has to be performed.
     * @throws StringSubstitutionException If any error occurs during substitution.
     */
    void substituteGroups(List<String> groups) throws StringSubstitutionException;
}
