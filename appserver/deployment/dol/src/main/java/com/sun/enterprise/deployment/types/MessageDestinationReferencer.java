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

import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;

/**
 * @author Kenneth Saks
 */
public interface MessageDestinationReferencer {

    /**
     * @return true if this referencer is linked to a message destination
     *         and false otherwise.
     */
    boolean isLinkedToMessageDestination();

    /**
     * Gets the link name of the reference. Points to the associated
     * message destination within the J2EE application. Can be NULL
     * if link is not set.
     *
     * @return the link name.
     */
    String getMessageDestinationLinkName();

    /**
     * Sets the link name of the reference. Points to the associated
     * message destination within the J2EE application. Can be NULL
     * if link is not set.
     *
     * @param the link name.
     */
    void setMessageDestinationLinkName(String linkName);

    /**
     * Sets the name of the message destination to which I refer.
     *
     * @param resolve if true, *try* to resolve link to the target message
     *            destination.
     * @return MessageDestination to which link was resolved, or null if
     *         link name resolution failed.
     */
    MessageDestinationDescriptor setMessageDestinationLinkName(String linkName, boolean resolve);

    /**
     * Try to resolve the current link name value to a MessageDestination
     * object.
     *
     * @return MessageDestination to which link was resolved, or null if
     *         link name resolution failed.
     */
    MessageDestinationDescriptor resolveLinkName();

    /**
     * @return the message destination object to which this message destination
     *         ref is linked. Can be NULL.
     */
    MessageDestinationDescriptor getMessageDestination();

    /**
     * @param destination set the message destination object to which this
     *            message destination ref is linked. Can be NULL.
     */
    void setMessageDestination(MessageDestinationDescriptor destination);

    /**
     * True if the owner is a message destination reference.
     */
    boolean ownedByMessageDestinationRef();

    /**
     * Get the descriptor for the message destination reference owner.
     */
    MessageDestinationReferenceDescriptor getMessageDestinationRefOwner();

    /**
     * True if the owner is a message-driven bean.
     */
    boolean ownedByMessageBean();

    /**
     * Get the descriptor for the message-driven bean owner.
     */
    EjbMessageBeanDescriptor getMessageBeanOwner();

}

