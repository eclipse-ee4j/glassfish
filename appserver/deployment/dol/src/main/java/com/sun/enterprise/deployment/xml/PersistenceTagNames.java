/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

    String PERSISTENCE = "persistence";
    String PERSISTENCE_UNIT = "persistence-unit";
    String DESCRIPTION = "description";
    String PROVIDER = "provider";
    String JTA_DATA_SOURCE = "jta-data-source";
    String NON_JTA_DATA_SOURCE = "non-jta-data-source";
    String JAR_FILE = "jar-file";
    String MAPPING_FILE = "mapping-file";
    String CLASS = "class";
    String EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";
    String PROPERTIES = "properties";
    String PROPERTY = "property";
    String PROPERTY_NAME = "name";
    String PROPERTY_VALUE = "value";
    String NAME = "name";
    String TRANSACTION_TYPE = "transaction-type";
    String SHARED_CACHE_MODE = "shared-cache-mode";
    String VALIDATION_MODE = "validation-mode";
    String SCOPE = "scope";
    String QUALIFIER = "qualifier";
    String PACKAGE_DESCRIPTOR = "package-descriptor";
    String MODULE_DESCRIPTOR = "module-descriptor";
    String DEFAULT_TO_ONE_FETCH_TYPE = "default-to-one-fetch-type";

}
