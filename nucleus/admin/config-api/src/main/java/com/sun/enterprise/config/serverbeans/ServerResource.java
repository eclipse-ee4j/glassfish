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

package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;

import java.beans.PropertyVetoException;

/**
 * Tag interface for all types of resource.
 *
 * @author Michael Cico
 */
@Configured
public interface ServerResource extends Named, Resource {

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    void setEnabled(String value) throws PropertyVetoException;

    @DuckTyped
    @Override
    String getIdentity();

    class Duck {
        public static String getIdentity(ServerResource resource) {
            return resource.getName();
        }
    }
}
