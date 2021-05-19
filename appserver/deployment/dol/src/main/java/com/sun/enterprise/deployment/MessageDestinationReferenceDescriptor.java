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

import com.sun.enterprise.deployment.types.MessageDestinationReference;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;


/**
 * An object representing the use of a message destination in a
 * J2EE component.
 *
 * @author Kenneth Saks
 *
*/

public class MessageDestinationReferenceDescriptor extends EnvironmentProperty
    implements MessageDestinationReference {

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    // Usage types
    public static final String CONSUMES = "Consumes";
    public static final String PRODUCES = "Produces";
    public static final String CONSUMES_PRODUCES = "ConsumesProduces";

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(MessageDestinationReferenceDescriptor.class);

    private BundleDescriptor referringBundle;

    private String usage;

    private String destinationType;

    // JNDI name of physical destination
    private String jndiName;

    // Holds information about the destination to which we are linked.
    private MessageDestinationReferencerImpl referencer;

    /**
    * Constructs a reference in the extrernal state.
    */

    public MessageDestinationReferenceDescriptor() {
        referencer = new MessageDestinationReferencerImpl(this);
    }

    /**
     * @return the usage type of the message destination reference
     * (Consumes, Produces, ConsumesProduces)
     */
    public String getUsage() {
        return usage;
    }

    /**
     * @param usage the usage type of the message destination reference
     * (Consumes, Produces, ConsumesProduces)
     */
    public void setUsage(String destUsage) {
        usage = destUsage;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String type) {
        destinationType = type;
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

    public String getInjectResourceType() {
        return getDestinationType();
    }

    public void setInjectResourceType(String resourceType) {
        setDestinationType(resourceType);
    }

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * message destination reference is declared.
     */
    public void setReferringBundleDescriptor(BundleDescriptor referringBundle) {
        this.referringBundle = referringBundle;
    }

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * message destination reference is declared.
     */
    public BundleDescriptor getReferringBundleDescriptor() {
        return referringBundle;
    }

    //
    // Implementations of MessageDestinationReferencer methods.
    //

    public boolean isLinkedToMessageDestination() {
        return referencer.isLinkedToMessageDestination();
    }

    /**
     * @return the name of the message destination to which I refer
     */
    public String getMessageDestinationLinkName() {
        return referencer.getMessageDestinationLinkName();
    }

    /**
     * Sets the name of the message destination to which I refer.
     */
    public void setMessageDestinationLinkName(String linkName) {
        referencer.setMessageDestinationLinkName(linkName);
    }


    public MessageDestinationDescriptor setMessageDestinationLinkName(String linkName, boolean resolveLink) {
        return referencer.setMessageDestinationLinkName(linkName, resolveLink);
    }

    public MessageDestinationDescriptor resolveLinkName() {
        return referencer.resolveLinkName();
    }

    public boolean ownedByMessageDestinationRef() {
        return true;
    }

    /**
     * Get the descriptor for the message destination reference owner.
     */
    public MessageDestinationReferenceDescriptor getMessageDestinationRefOwner() {
        return this;
    }

    /**
     * True if the owner is a message-driven bean.
     */
    public boolean ownedByMessageBean() {
        return false;
    }

    /**
     * Get the descriptor for the message-driven bean owner.
     */
    public EjbMessageBeanDescriptor getMessageBeanOwner() {
        return null;
    }

    /**
     * @return the message destination to which I refer. Can be NULL.
    */
    public MessageDestinationDescriptor getMessageDestination() {
        return referencer.getMessageDestination();
    }

    /**
     * @param messageDestiation the message destination to which I refer.
     */
    public void setMessageDestination(MessageDestinationDescriptor newMsgDest) {
        referencer.setMessageDestination(newMsgDest);
    }


    /**
     * returns a formatted string representing me.
     */

    public void print(StringBuffer toStringBuffer) {
        if (isLinkedToMessageDestination()) {
            toStringBuffer.append("Resolved Message-Destination-Ref ").append(getName())
                .append("points to logical message destination ").append(getMessageDestination().getName());
        } else {
            toStringBuffer.append("Unresolved Message-Destination-Ref ").append(getName()).append("@").append(getType())
                .append("@").append(usage);
        }
    }

    public boolean isConflict(MessageDestinationReferenceDescriptor other) {
        return getName().equals(other.getName()) &&
            (!(
                DOLUtils.equals(getDestinationType(), other.getDestinationType()) &&
                DOLUtils.equals(getUsage(), other.getUsage()) &&
                DOLUtils.equals(getMessageDestinationLinkName(), other.getMessageDestinationLinkName())
                ) ||
            isConflictResourceGroup(other));
    }

    /* Equality on name. */
    public boolean equals(Object object) {
        if (object instanceof MessageDestinationReference) {
            MessageDestinationReference reference = (MessageDestinationReference) object;
            return reference.getName().equals(this.getName());
        }
        return false;
    }


    public int hashCode() {
        int result = NULL_HASH_CODE;
        String name = getName();
        if (name != null) {
            result += name.hashCode();
        }
        return result;
    }
}
