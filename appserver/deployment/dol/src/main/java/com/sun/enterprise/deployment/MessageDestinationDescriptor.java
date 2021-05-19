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

package com.sun.enterprise.deployment;

/**
 * This class represents information about a web service
 * endpoint.
 *
 * @author Kenneth Saks
 */

import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import org.glassfish.deployment.common.Descriptor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MessageDestinationDescriptor extends Descriptor implements NamedDescriptor{

    private String msgDestName;

    // JNDI name of physical destination to which this logical
    // destination is mapped.
    private String jndiName;
    private String mappedName;
    private String lookupName;

    // Set of MessageDestinationReferencer descriptors pointing to me.
    private Set referencers = new HashSet();

    // bundle in which I am defined
    private BundleDescriptor bundleDescriptor;

    public MessageDestinationDescriptor() {
    }

    public MessageDestinationDescriptor(String name, String description) {
        super("", description);
        msgDestName = name;
    }

    public boolean hasName() {
        return (msgDestName != null);
    }

    public void setName(String name) {
        msgDestName = name;
    }

    public String getName() {
        return msgDestName;
    }

    public void setDisplayName(String displayName) {
        setLocalizedDisplayName(null, displayName);
    }

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
        if( bundleDesc == null ) {
            for(Iterator iter = referencers.iterator(); iter.hasNext();) {
                MessageDestinationReferencer next =
                    (MessageDestinationReferencer) iter.next();
                next.setMessageDestination(null);
            }
            referencers.clear();
        }
        bundleDescriptor = bundleDesc;
    }

    public String getJndiName() {
        if (jndiName != null  && ! jndiName.equals("")) {
            return jndiName;
        }
        if (mappedName != null && ! mappedName.equals("")) {
            return mappedName;
        }
        return lookupName;
    }

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
        return (lookupName != null)? lookupName : "";
    }

}
