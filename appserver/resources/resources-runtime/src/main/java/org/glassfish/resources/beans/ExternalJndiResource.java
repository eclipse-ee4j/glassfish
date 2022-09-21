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

package org.glassfish.resources.beans;

import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.JavaEEResourceBase;

/**
 * Resource info for ExternalJndiResource.
 * IASRI #4626188
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 *
 * @author Sridatta Viswanath
 */
public class ExternalJndiResource extends JavaEEResourceBase {

    private static final long serialVersionUID = 1L;
    private String jndiLookupName_;
    private String resType_;
    private String factoryClass_;

    public ExternalJndiResource(ResourceInfo resourceInfo) {
        super(resourceInfo);
    }

    @Override
    protected JavaEEResource doClone(ResourceInfo resourceInfo) {
        ExternalJndiResource clone = new ExternalJndiResource(resourceInfo);
        clone.setJndiLookupName(getJndiLookupName());
        clone.setResType(getResType());
        clone.setFactoryClass(getFactoryClass());
        return clone;
    }

    @Override
    public int getType() {
        return JavaEEResource.EXTERNAL_JNDI_RESOURCE;
    }

    public String getJndiLookupName() {
        return jndiLookupName_;
    }

    public void setJndiLookupName(String jndiLookupName) {
        jndiLookupName_ = jndiLookupName;
    }

    public String getResType() {
        return resType_;
    }

    public void setResType(String resType) {
        resType_ = resType;
    }

    public String getFactoryClass() {
        return factoryClass_;
    }

    public void setFactoryClass(String factoryClass) {
        factoryClass_ = factoryClass;
    }

    //START OF IASRI 4660565
    public boolean isJMSConnectionFactory() {
        if (resType_ == null) {
            return false;
        }

        return ResourceConstants.JMS_QUEUE_CONNECTION_FACTORY.equals(resType_)
            || ResourceConstants.JMS_TOPIC_CONNECTION_FACTORY.equals(resType_);
    }
    //END OF IASRI 4660565

    @Override
    public String toString() {
        return "< External Jndi Resource : " + getResourceInfo() + " , " + getJndiLookupName() + "... >";
    }
}
