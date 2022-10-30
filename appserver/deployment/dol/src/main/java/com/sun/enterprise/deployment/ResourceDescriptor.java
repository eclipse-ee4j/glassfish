/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * @author naman
 */
public class ResourceDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;

    private MetadataSource metadataSource = MetadataSource.XML;
    private String resourceId;
    private JavaEEResourceType resourceType;

    public ResourceDescriptor() {
    }


    public ResourceDescriptor(ResourceDescriptor resourceDescriptor) {
        super(resourceDescriptor);
    }


    public String getResourceId() {
        return resourceId;
    }


    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    public JavaEEResourceType getResourceType() {
        return resourceType;
    }


    public final void setResourceType(JavaEEResourceType resourceType) {
        this.resourceType = resourceType;
    }


    public final SimpleJndiName getJndiName() {
        return SimpleJndiName.of(getName());
    }


    public MetadataSource getMetadataSource() {
        return metadataSource;
    }


    public void setMetadataSource(MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }


    public static String getJavaComponentJndiName(String name) {
        if (name == null) {
            return null;
        }
        // some names really contain java: in the middle...?
        if (name.contains(SimpleJndiName.JNDI_CTX_JAVA)) {
            return name;
        }
        return SimpleJndiName.JNDI_CTX_JAVA_COMPONENT + name;
    }
}
