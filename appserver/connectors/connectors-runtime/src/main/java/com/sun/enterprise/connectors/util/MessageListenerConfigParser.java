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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.util.Properties;

/** Interface class of managed connection factory parser methods.
 *  @author Srikanth P
 */
public interface MessageListenerConfigParser extends ConnectorConfigParser {


    /**
     *  Obtains the Message Listener types of a given rar.
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @return Array of MessageListener types as strings
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    String[] getMessageListenerTypes(ConnectorDescriptor desc)
                      throws ConnectorRuntimeException;

    /**
     *  Returns the ActivationSpecClass name for the given rar and message
     *  listener type.
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @param messageListenerType MessageListener type
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    String getActivationSpecClass(ConnectorDescriptor desc,
          String messageListenerType) throws ConnectorRuntimeException;

    /**
     * Returns the Properties object consisting of PropertyName as the key
     * and the datatype as the value
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @param  messageListenerType message listener type.It is uniqie
     *          across all <messagelistener> sub-elements in <messageadapter>
     *          element in a given rar.
     *  @return properties object with the property names(key) and datatype
     *          of property(as value).
     *  @throws  ConnectorRuntimeException if either of the parameters are null.
     *           If corresponding rar is not deployed i.e moduleDir is invalid.
     *           If messagelistener type is not found in ra.xml
     */
    Properties getJavaBeanReturnTypes(ConnectorDescriptor desc,
          String messageListenerType, String rarName) throws ConnectorRuntimeException;
}
