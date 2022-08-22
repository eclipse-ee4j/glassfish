/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.PermissionItemDescriptor;
import com.sun.enterprise.deployment.PermissionsDescriptor;
import com.sun.enterprise.deployment.xml.DeclaredPermissionsTagNames;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PermissionsNode extends AbstractBundleNode<PermissionsDescriptor> {

    private static final String SCHEMA_ID = "permissions_9.xsd";
    private static final String SPEC_VERSION = "9";
    private static final List<String> systemIDs = List.of(SCHEMA_ID);

    // The XML tag associated with this Node
    public final static XMLElement ROOT_ELEMENT = new XMLElement(DeclaredPermissionsTagNames.PERMS_ROOT);

    private PermissionsDescriptor permDescriptor;

    public PermissionsNode() {
        if (handlers != null) {
            // FIXME: make it a parameter of parent
            handlers.clear();
        }
        permDescriptor = new PermissionsDescriptor();
        registerElementHandler(new XMLElement(DeclaredPermissionsTagNames.PERM_ITEM), PermissionItemNode.class);
        SaxParserHandler.registerBundleNode(this, DeclaredPermissionsTagNames.PERMS_ROOT);
    }


    public PermissionsNode(PermissionsDescriptor permDescriptor) {
        this();
        this.permDescriptor = permDescriptor;
    }


    @Override
    public PermissionsDescriptor getDescriptor() {
        return permDescriptor;
    }


    @Override
    public String registerBundle(Map<String, String> publicIDToSystemIDMapping) {
        return ROOT_ELEMENT.getQName();
    }


    @Override
    public Map<String, Class<?>> registerRuntimeBundle(Map<String, String> publicIDToSystemIDMapping,
        Map<String, List<Class<?>>> versionUpgrades) {
        return Collections.emptyMap();
    }


    @Override
    public String getDocType() {
        return null;
    }


    @Override
    public String getSystemID() {
        return SCHEMA_ID;
    }


    @Override
    public List getSystemIDs() {
        return systemIDs;
    }


    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return ROOT_ELEMENT;
    }


    @Override
    public void addDescriptor(Object descriptor) {
        if (descriptor instanceof PermissionItemDescriptor) {
            final PermissionItemDescriptor pid = PermissionItemDescriptor.class.cast(descriptor);
            this.getDescriptor().addPermissionItemdescriptor(pid);
        }
    }
}
