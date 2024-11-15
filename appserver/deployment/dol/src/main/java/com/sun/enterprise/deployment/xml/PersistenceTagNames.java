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

package com.sun.enterprise.deployment.xml;

/**
 * These names match names defined in persistence.xsd file
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface PersistenceTagNames extends TagNames {

    public static String PERSISTENCE = "persistence";
    public static String PERSISTENCE_UNIT = "persistence-unit";
    public static String DESCRIPTION = "description";
    public static String PROVIDER = "provider";
    public static String JTA_DATA_SOURCE = "jta-data-source";
    public static String NON_JTA_DATA_SOURCE = "non-jta-data-source";
    public static String JAR_FILE = "jar-file";
    public static String MAPPING_FILE = "mapping-file";
    public static String CLASS = "class";
    public static String EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";
    public static String PROPERTIES = "properties";
    public static String PROPERTY = "property";
    public static String PROPERTY_NAME = "name";
    public static String PROPERTY_VALUE = "value";
    public static String NAME = "name";
    public static String TRANSACTION_TYPE = "transaction-type";
    public static String SHARED_CACHE_MODE = "shared-cache-mode";
    public static String VALIDATION_MODE = "validation-mode";
    public static String SCOPE = "scope";
    public static String QUALIFIER = "qualifier";

}
