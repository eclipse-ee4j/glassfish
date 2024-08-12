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

package com.sun.enterprise.deployment.runtime;

import com.sun.enterprise.deployment.BundleDescriptor;

import org.glassfish.deployment.common.Descriptor;

/**
 *Records information about Java Web Start access to an app client.
 * @author tjquinn
 */
public class JavaWebStartAccessDescriptor extends Descriptor {

    private BundleDescriptor bundleDescriptor;

    /** Creates a new instance of JavaWebStartAccessDescriptor */
    public JavaWebStartAccessDescriptor() {
    }

    /**
     * Holds value of property eligible.
     */
    private boolean eligible = true;

    /**
     * Getter for property eligible.
     * @return Value of property eligible.
     */
    public boolean isEligible() {

        return this.eligible;
    }

    /**
     * Setter for property eligible.
     * @param eligible New value of property eligible.
     */
    public void setEligible(boolean eligible) {

        this.eligible = eligible;
    }

    /**
     * Holds value of property contextRoot.
     */
    private String contextRoot = null;

    /**
     * Getter for property contextRoot.
     * @return Value of property contextRoot.
     */
    public String getContextRoot() {

        return this.contextRoot;
    }

    /**
     * Setter for property contextRoot.
     * @param contextRoot New value of property contextRoot.
     */
    public void setContextRoot(String contextRoot) {

        this.contextRoot = contextRoot;
    }

    /**
     * Holds value of property vendor.
     */
    private String vendor = null;

    /**
     * Getter for property vendor.
     * @return Value of property vendor.
     */
    public String getVendor() {

        return this.vendor;
    }

    /**
     * Setter for property vendor.
     * @param contextRoot New value of property vendor.
     */
    public void setVendor(String vendor) {

        this.vendor = vendor;
    }

     public void setBundleDescriptor(BundleDescriptor bundle) {
        bundleDescriptor = bundle;
    }

    public BundleDescriptor getBundleDescriptor() {
        return bundleDescriptor;
    }

    /**
     * Declaration and methods for jnlp-doc subelement.
     */
    private String jnlpDoc = null;

    public String getJnlpDocument() {
        return jnlpDoc;
    }

    public void setJnlpDocument(String jnlpDoc) {
        this.jnlpDoc = jnlpDoc;
    }


}
