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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.types.MessageDestinationReferencer;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class represents information about a web service endpoint.
 *
 * @author Kenneth Saks
 */
public class MessageDestinationDescriptor extends Descriptor implements NamedDescriptor{

    private static final long serialVersionUID = 1L;

    private String msgDestName;

    // JNDI name of physical destination to which this logical
    // destination is mapped.
    private String jndiName;
    private String mappedName;
    private String lookupName;

    // Set of MessageDestinationReferencer descriptors pointing to me.
    private final Set<MessageDestinationReferencer> referencers = new HashSet<>();

    // bundle in which I am defined
    private BundleDescriptor bundleDescriptor;

    public MessageDestinationDescriptor() {
    }

    public MessageDestinationDescriptor(String name, String description) {
        super("", description);
        msgDestName = name;
    }

    public boolean hasName() {
        return msgDestName != null;
    }

    @Override
    public void setName(String name) {
        msgDestName = name;
    }

    @Override
    public String getName() {
        return msgDestName;
    }

    @Override
    public void setDisplayName(String displayName) {
        setLocalizedDisplayName(null, displayName);
    }

    @Override
    public String getDisplayName() {
        return getLocalizedDisplayName(null);
    }

    public Set getAllReferencers() {
        return referencers;
    }

    public void addReferencer(MessageDestinationReferencer referencer) {
        referencers.add(referencer);
    }

    public void removeReferencer(MessageDestinationReferencer referencer) {
        referencers.remove(referencer);
    }

    public BundleDescriptor getBundleDescriptor() {
        return bundleDescriptor;
    }

    public void setBundleDescriptor(BundleDescriptor bundleDesc) {
        if (bundleDesc == null) {
            for (MessageDestinationReferencer referencer : referencers) {
                referencer.setMessageDestination(null);
            }
            referencers.clear();
        }
        bundleDescriptor = bundleDesc;
    }

    @Override
    public String getJndiName() {
        if (jndiName != null && !jndiName.isEmpty()) {
            return jndiName;
        }
        if (mappedName != null && !mappedName.isEmpty()) {
            return mappedName;
        }
        return lookupName;
    }


    @Override
    public void setJndiName(String physicalDestinationName) {
        jndiName = physicalDestinationName;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public void setLookupName(String lName) {
        lookupName = lName;
    }

    public String getLookupName() {
        return lookupName == null ? "" : lookupName;
    }

}
