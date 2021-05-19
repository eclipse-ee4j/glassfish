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

package com.sun.enterprise.deployment.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.enterprise.deployment.xml.DeclaredPermissionsTagNames;
import com.sun.enterprise.deployment.PermissionItemDescriptor;
import com.sun.enterprise.deployment.PermissionsDescriptor;

public class PermissionsNode extends AbstractBundleNode {

    public final static String SCHEMA_ID = "permissions_9.xsd";
    public final static String SPEC_VERSION = "9";

    private final static List<String> systemIDs = initSystemIDs();

    // The XML tag associated with this Node
    public final static XMLElement ROOT_ELEMENT = new XMLElement(
            DeclaredPermissionsTagNames.PERMS_ROOT);

    private final static List<String> initSystemIDs() {

        List<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    private PermissionsDescriptor permDescriptor;


    public PermissionsNode() {
        if (handlers != null) handlers.clear();

        permDescriptor = new PermissionsDescriptor();

        registerElementHandler(
                new XMLElement(DeclaredPermissionsTagNames.PERM_ITEM),
                PermissionItemNode.class);

        SaxParserHandler.registerBundleNode(this,
                DeclaredPermissionsTagNames.PERMS_ROOT);
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
    public Map<String, Class> registerRuntimeBundle(
            Map<String, String> publicIDToSystemIDMapping,
            final Map<String, List<Class>> versionUpgrades) {

        return Collections.EMPTY_MAP;
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

    protected XMLElement getXMLRootTag() {
        return ROOT_ELEMENT;
    }

    @Override
    public void addDescriptor(Object descriptor) {

        if (descriptor instanceof PermissionItemDescriptor) {
            final PermissionItemDescriptor pid =
                PermissionItemDescriptor.class.cast(descriptor);
            this.getDescriptor().addPermissionItemdescriptor(pid);
        }
    }
}
