/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.xml.PersistenceTagNames;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * This node is responsible for reading details about one <persistence-unit/>
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitNode extends DeploymentDescriptorNode {

    /**
     * map of element names to method names in {@link PersistenceUnitDescriptor}
     */
    private Map<String, String> dispatchTable;

    /**
     * This is the default constructor which is also called from other
     * constructors of this class. Inside this constructor, we clear the
     * handlers set up by super classes' constructors because they are
     * not applicable in the context of PersistenceNode because
     * unlike standard Jakarta EE schemas, persistence.xsd does not include
     * jakartaee xsd for things like description, version etc.
     */
    public PersistenceUnitNode() {
        // clear all the handlers set up by super classes
        // because that sets up a handler for description which we are not
        // interested in.
        if (handlers != null) {
            handlers.clear();
        }
        initDispatchTable();
    }

    @Override public void startElement(
            XMLElement element, Attributes attributes) {
        if (PersistenceTagNames.PROPERTY.equals(element.getQName())) {
            assert(attributes.getLength() == 2);
            assert(attributes.getIndex(PersistenceTagNames.PROPERTY_NAME) !=
                    -1);
            assert(attributes.getIndex(PersistenceTagNames.PROPERTY_VALUE) !=
                    -1);
            PersistenceUnitDescriptor persistenceUnitDescriptor = (PersistenceUnitDescriptor) getDescriptor();
            String propName = attributes.getValue(
                    PersistenceTagNames.PROPERTY_NAME);
            String propValue = attributes.getValue(
                    PersistenceTagNames.PROPERTY_VALUE);
            persistenceUnitDescriptor.addProperty(propName, propValue);
            return;
        }
        super.startElement(element, attributes);
    }

    /**
     * This returns the dispatch table for this node.
     * Please note, unlike Jakarta EE schemas persistence.xsd does not include
     * standard elements or attributes (e.g. version, descriptionGroupRef etc.)
     * from jakartaee xsd, we don't use super classes' dispatch table.
     * @return map of element names to method names in PersistenceUnitDescriptor
     * @see DeploymentDescriptorNode#getDispatchTable()
     * @see #initDispatchTable()
     */
    @Override
    protected Map getDispatchTable() {
        return dispatchTable;
    }

    /**
     * Please note, unlike Jakarta EE schemas persistence.xsd does not include
     * standard elements or attributes (e.g. version, descriptionGroupRef etc.)
     * from jakartaee xsd, we don't use super classes' dispatch table.
     */
    private void initDispatchTable() {
        assert(dispatchTable == null);

        // we don't do super.getDispatchTable() because we are not
        // interested in any of super classes' disptcah table entries.
        Map<String, String> table = new HashMap<>();

        // the values being put into the map represent method names
        // in PersistenceUnitDescriptor class.
        table.put(PersistenceTagNames.NAME, "setName");
        table.put(PersistenceTagNames.TRANSACTION_TYPE, "setTransactionType");
        table.put(PersistenceTagNames.DESCRIPTION, "setDescription");
        table.put(PersistenceTagNames.PROVIDER, "setProvider");
        table.put(PersistenceTagNames.JTA_DATA_SOURCE, "setJtaDataSource");
        table.put(PersistenceTagNames.NON_JTA_DATA_SOURCE, "setNonJtaDataSource");
        table.put(PersistenceTagNames.MAPPING_FILE, "addMappingFile");
        table.put(PersistenceTagNames.JAR_FILE, "addJarFile");
        table.put(PersistenceTagNames.EXCLUDE_UNLISTED_CLASSES, "setExcludeUnlistedClasses");
        table.put(PersistenceTagNames.CLASS, "addClass");
        table.put(PersistenceTagNames.SHARED_CACHE_MODE, "setSharedCacheMode");
        table.put(PersistenceTagNames.VALIDATION_MODE, "setValidationMode");
        table.put(PersistenceTagNames.SCOPE, "setValidationMode");
        table.put(PersistenceTagNames.QAULIFIER, "addQualifier");
        this.dispatchTable = table;
    }

}
