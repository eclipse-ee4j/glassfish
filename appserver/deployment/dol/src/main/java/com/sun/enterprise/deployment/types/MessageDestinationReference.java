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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.BundleDescriptor;

/**
 * Protocol associated with defining an EJB Interface
 *
 * @author Kenneth Saks
 */

public interface MessageDestinationReference extends MessageDestinationReferencer, NamedInformation {

    String CONSUMES = "Consumes";
    String PRODUCES = "Produces";
    String CONSUMES_AND_PRODUCES = "ConsumesProduces";

    /**
     * @return the type of the message destination reference
     */
    String getType();

    /**
     * @param type the type of the message destination reference
     */
    void setType(String type);

    /**
     * @return the usage type of the message destination reference
     *         (Consumes, Produces, ConsumesProduces)
     */
    String getUsage();

    /**
     * @param usage the usage type of the message destination reference
     *            (Consumes, Produces, ConsumesProduces)
     */
    void setUsage(String usage);

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * message destination reference is declared.
     */
    void setReferringBundleDescriptor(BundleDescriptor referringBundle);

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * message destinaion reference is declared.
     */
    BundleDescriptor getReferringBundleDescriptor();

}

