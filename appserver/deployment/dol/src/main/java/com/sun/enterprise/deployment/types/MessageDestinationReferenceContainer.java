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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;

import java.util.Set;

/**
 * This type defines the behaviour of a J2EE Component containing
 * message destination references.
 *
 * @author Kennneth Saks
 * @version
 */
public interface MessageDestinationReferenceContainer {

    /**
     * Add a message destination reference to myself
     *
     * @param the new message destination ref
     */
    void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestReference);

    /**
     * Return a message destination reference by the same name or throw an IllegalArgumentException.
     *
     * @param the message destination reference name
     */
    MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name);

    /**
     * Return the set of message destination references declared.
     */
    Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors();

    /**
     * Removes the specified message destination reference from the receiver.
     *
     * @param ref MessageDestinationReferenceDescriptor to remove.
     */
    void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef);
}

