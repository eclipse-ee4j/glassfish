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

package org.glassfish.resources.beans;

import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.JavaEEResourceBase;

/**
 * Resource info for CustomResourcel.
 * IASRI #4626188
 *
 * @author Sridatta Viswanath
 */
public class CustomResource extends JavaEEResourceBase {

    private String resType_;
    private String factoryClass_;

    public CustomResource(ResourceInfo resourceInfo) {
        super(resourceInfo);
    }

    protected JavaEEResource doClone(ResourceInfo resourceInfo) {
        CustomResource clone = new CustomResource(resourceInfo);
        clone.setResType(getResType());
        clone.setFactoryClass(getFactoryClass());
        return clone;
    }

    public int getType() {
        return JavaEEResource.CUSTOM_RESOURCE;
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

    public String toString() {
        return "< Custom Resource : " + getResourceInfo() + " , " + getResType() + "... >";
    }
}
