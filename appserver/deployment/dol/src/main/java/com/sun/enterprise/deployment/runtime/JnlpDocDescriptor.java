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

/**
 * Descriptor definition for
 * <sun-application-client>
 *   <java-web-start-support>
 *     <jnlp-ref>
 *
 * @author tjquinn
 */
public class JnlpDocDescriptor extends RuntimeDescriptor {

    public static final String HREF = "href";

    private BundleDescriptor bundleDescriptor;
    private String href;

    public void setBundleDescriptor(BundleDescriptor bundle) {
        bundleDescriptor = bundle;
    }

    public BundleDescriptor getBundleDescriptor() {
        return bundleDescriptor;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
